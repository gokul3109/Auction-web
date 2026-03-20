package com.auctionweb.auction.service;

import com.auctionweb.auction.dto.BidResponse;
import com.auctionweb.auction.model.Auction;
import com.auctionweb.auction.model.Bid;
import com.auctionweb.auction.repository.AuctionRepository;
import com.auctionweb.auction.repository.BidRepository;
import com.auctionweb.auction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for managing the auction lifecycle.
 * 
 * Key responsibilities:
 * 1. Automatically end auctions when their endDate expires
 * 2. Determine winners (highest bidder)
 * 3. Change auction status from "active" to "completed"
 * 4. Send notifications to winners and losers
 * 
 * Runs periodically using @Scheduled to check for expired auctions.
 */
@Service
public class AuctionEndingService {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private com.auctionweb.auction.service.AuctionEventService auctionEventService;

    /**
     * Ensure an auction is ended if its endDate has passed.
     * This is called ON-DEMAND whenever someone accesses the auction.
     * 
     * If auction has expired but still "active", immediately end it.
     * This prevents the 30-second gap where expired auctions appear active.
     * 
     * The scheduler is still a safety net for auctions nobody accesses.
     */
    public void ensureAuctionNotExpired(UUID auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        // If auction is not active, nothing to do
        if (!"active".equals(auction.getStatus())) {
            return;
        }

        // If endDate hasn't passed yet, nothing to do
        if (auction.getEndDate() == null || auction.getEndDate().isAfter(LocalDateTime.now())) {
            return;
        }

        // Auction has expired - end it NOW
        endAuction(auction);
    }

    /**
     * Scheduled task that runs every 30 seconds to check for expired auctions.
     * When an auction's endDate has passed and it's still "active", this:
     * 1. Changes status to "completed"
     * 2. Determines the winner (highest bidder)
     * 3. Could trigger notifications (future enhancement)
     * 
     * Note: This runs asynchronously in the background.
     * The ensureAuctionNotExpired() method handles on-demand expiration
     * so users see accurate status immediately.
     */
    @Scheduled(fixedDelay = 30000) // Every 30 seconds
    @Transactional
    public void checkAndEndExpiredAuctions() {
        // Find all ACTIVE auctions where endDate has passed
        LocalDateTime now = LocalDateTime.now();
        List<Auction> expiredAuctions = auctionRepository.findByStatusAndEndDateBefore("active", now);

        if (expiredAuctions.isEmpty()) {
            return; // No expired auctions
        }

        for (Auction auction : expiredAuctions) {
            try {
                endAuction(auction);
            } catch (Exception e) {
                // Log error but continue processing other auctions
                System.err.println("Error ending auction " + auction.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Ends a single auction, determining winner and updating status.
     * Also broadcasts auction-ended event to all clients watching via SSE.
     */
    @Transactional
    public void endAuction(Auction auction) {
        // Change status to completed
        auction.setStatus("completed");
        auctionRepository.save(auction);

        // Log event (for debugging)
        System.out.println("[AUCTION_ENDED] Auction " + auction.getId() + " (" + auction.getTitle() + ") has expired.");
        
        // Get winner if any bids exist
        Optional<Bid> winningBid = getWinningBid(auction.getId());
        
        // Broadcast auction-ended event to all clients watching this auction
        broadcastAuctionEnded(auction.getId(), winningBid);
        
        if (winningBid.isPresent()) {
            Bid bid = winningBid.get();
            System.out.println("[AUCTION_WON] User " + bid.getUserId() + " won with bid of " + bid.getBidAmount());

            // In-app notification to winner
            notificationService.notifyAuctionWon(bid.getUserId(), auction.getTitle(), bid.getBidAmount(), auction.getId());
            // In-app notification to seller
            notificationService.notifyAuctionSold(auction.getUserId(), auction.getTitle(), bid.getBidAmount(), auction.getId());

            String auctionUrl = "http://localhost:3000/auctions/" + auction.getId();
            // Email to winner
            userRepository.findById(bid.getUserId()).ifPresent(winner ->
                sendEmailSilently(() -> emailService.sendAuctionWonEmail(
                    winner.getEmail(), auction.getTitle(), bid.getBidAmount().toString(), auctionUrl))
            );
            // Email to seller
            userRepository.findById(auction.getUserId()).ifPresent(seller ->
                sendEmailSilently(() -> emailService.sendAuctionSoldEmail(
                    seller.getEmail(), auction.getTitle(), bid.getBidAmount().toString(), auctionUrl))
            );
        } else {
            System.out.println("[NO_BIDS] Auction " + auction.getId() + " ended with no bids.");

            // In-app notification to seller
            notificationService.notifyAuctionNoBids(auction.getUserId(), auction.getTitle(), auction.getId());
            // Email to seller
            userRepository.findById(auction.getUserId()).ifPresent(seller ->
                sendEmailSilently(() -> emailService.sendAuctionNoBidsEmail(seller.getEmail(), auction.getTitle()))
            );
        }
    }

    private void sendEmailSilently(Runnable emailTask) {
        try {
            emailTask.run();
        } catch (Exception e) {
            System.err.println("[EMAIL_ERROR] Failed to send email: " + e.getMessage());
        }
    }

    /**
     * Broadcast auction-ended event to all clients watching this auction via SSE.
     * Updates all browsers in real-time when auction expires.
     */
    private void broadcastAuctionEnded(UUID auctionId, Optional<Bid> winningBid) {
        try {
            java.util.Map<String, Object> event = new java.util.HashMap<>();
            event.put("type", "AUCTION_ENDED");
            event.put("auctionId", auctionId.toString());
            event.put("message", "This auction has ended");
            
            if (winningBid.isPresent()) {
                Bid bid = winningBid.get();
                event.put("hasWinner", true);
                event.put("winnerId", bid.getUserId().toString());
                event.put("winningAmount", bid.getBidAmount());
                event.put("winTime", bid.getCreatedAt());
            } else {
                event.put("hasWinner", false);
                event.put("message", "Auction ended with no bids");
            }
            
            // Send via SSE to all clients watching this auction
            auctionEventService.broadcastAuctionEnded(auctionId, event);
        } catch (Exception e) {
            // SSE broadcast failed, but auction was already ended in DB
            System.err.println("Failed to broadcast auction-ended event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get the winning bid for an auction (highest bid).
     * Returns empty if auction has no bids.
     */
    public Optional<Bid> getWinningBid(UUID auctionId) {
        // Get the highest bid (first result when ordered by bidAmount DESC)
        List<Bid> topBid = bidRepository.findByAuctionIdOrderByBidAmountDesc(auctionId);
        return topBid.isEmpty() ? Optional.empty() : Optional.of(topBid.get(0));
    }

    /**
     * Get winner information in DTO format (for API response).
     * Returns null if auction has no bids or is still active.
     */
    public AuctionWinnerInfo getWinnerInfo(UUID auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        // Only return winner info if auction is completed
        if (!"completed".equals(auction.getStatus())) {
            return null; // Auction still active, no winner yet
        }

        Optional<Bid> winningBid = getWinningBid(auctionId);

        if (winningBid.isEmpty()) {
            // Auction completed with no bids
            return AuctionWinnerInfo.noWinner(auctionId);
        }

        Bid bid = winningBid.get();
        return AuctionWinnerInfo.withWinner(
            auctionId,
            bid.getId(),
            bid.getUserId(),
            bid.getBidAmount(),
            bid.getCreatedAt()
        );
    }

    /**
     * Check if an auction has ended (status = "completed").
     */
    public boolean isAuctionEnded(UUID auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));
        return "completed".equals(auction.getStatus());
    }

    /**
     * Get time remaining for an auction.
     * Returns null if auction is already ended.
     * Returns negative value if auction has expired but not yet closed (shouldn't happen with scheduler running often).
     */
    public Long getTimeRemaining(UUID auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        if ("completed".equals(auction.getStatus())) {
            return null; // Already ended
        }

        LocalDateTime now = LocalDateTime.now();
        return java.time.temporal.ChronoUnit.SECONDS.between(now, auction.getEndDate());
    }

    /**
     * Get auction statistics including winner info, bid count, price range.
     */
    public AuctionStats getAuctionStats(UUID auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        List<Bid> allBids = bidRepository.findByAuctionIdOrderByBidAmountDesc(auctionId);

        return AuctionStats.builder()
                .auctionId(auctionId)
                .totalBids(allBids.size())
                .highestBid(allBids.isEmpty() ? null : allBids.get(0).getBidAmount())
                .lowestBid(allBids.isEmpty() ? null : allBids.get(allBids.size() - 1).getBidAmount())
                .averageBid(allBids.isEmpty() ? null : allBids.stream()
                        .map(Bid::getBidAmount)
                        .reduce((a, b) -> a.add(b))
                        .orElse(null)
                        .divide(new java.math.BigDecimal(allBids.size())))
                .uniqueBidders(allBids.stream().map(Bid::getUserId).distinct().count())
                .timeRemaining(getTimeRemaining(auctionId))
                .isActive("active".equals(auction.getStatus()))
                .isCompleted("completed".equals(auction.getStatus()))
                .winnerInfo(getWinnerInfo(auctionId))
                .build();
    }
}
