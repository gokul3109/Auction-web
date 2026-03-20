package com.auctionweb.auction.service;

import com.auctionweb.auction.dto.BidRequest;
import com.auctionweb.auction.dto.BidResponse;
import com.auctionweb.auction.model.Auction;
import com.auctionweb.auction.model.Bid;
import com.auctionweb.auction.repository.AuctionRepository;
import com.auctionweb.auction.repository.BidRepository;
import com.auctionweb.auction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BidService {

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private com.auctionweb.auction.service.AuctionEndingService auctionEndingService;

    /**
     * Place a bid on an auction.
     *
     * Rules enforced:
     *  1. Auction must exist
     *  2. Auction must be active (status = "active") and not expired
     *  3. User cannot bid on their own auction
     *  4. Bid amount must be strictly greater than the current price
     *  5. On success, update auction's currentPrice to the new bid amount
     */
    public BidResponse placeBid(UUID auctionId, BidRequest request, UUID userId) {
        // First: Check if auction has expired and end it immediately if so
        // This prevents the 30-second gap between expiration and scheduler running
        auctionEndingService.ensureAuctionNotExpired(auctionId);

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        // Rule 2: auction must be active
        if (!"active".equals(auction.getStatus())) {
            throw new RuntimeException("This auction has ended and is no longer accepting bids");
        }

        // Rule 2b: auction must not have passed its end date
        // (This should be redundant now after ensureAuctionNotExpired, but kept as safety check)
        if (auction.getEndDate() != null && auction.getEndDate().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("This auction has expired");
        }

        // Rule 3: cannot bid on own auction
        if (auction.getUserId().equals(userId)) {
            throw new RuntimeException("You cannot bid on your own auction");
        }

        // Rule 4: bid must be higher than current price
        if (request.getBidAmount().compareTo(auction.getCurrentPrice()) <= 0) {
            throw new RuntimeException(
                "Bid amount must be greater than the current price of " + auction.getCurrentPrice()
            );
        }

        // Save the bid
        Bid bid = new Bid();
        bid.setAuctionId(auctionId);
        bid.setUserId(userId);
        bid.setBidAmount(request.getBidAmount());
        Bid saved = bidRepository.save(bid);

        // Capture previous highest bidder BEFORE updating the price
        Optional<Bid> previousHighest = bidRepository.findByAuctionIdOrderByBidAmountDesc(auctionId)
                .stream()
                .filter(b -> !b.getUserId().equals(userId))
                .findFirst();

        // Update auction's current price
        auction.setCurrentPrice(request.getBidAmount());
        auctionRepository.save(auction);

        // Notify previous highest bidder (outbid) — skip if they're the same user
        previousHighest.ifPresent(prev -> {
            if (!prev.getUserId().equals(userId)) {
                notificationService.notifyOutbid(prev.getUserId(), auction.getTitle(), auctionId);
                userRepository.findById(prev.getUserId()).ifPresent(prevUser ->
                    sendEmailSilently(() -> emailService.sendOutbidEmail(
                        prevUser.getEmail(),
                        auction.getTitle(),
                        "http://localhost:3000/auctions/" + auctionId
                    ))
                );
            }
        });

        // Notify auction owner that a new bid was placed (skip if owner placed the bid — guarded above)
        String bidderName = userRepository.findById(userId)
                .map(u -> u.getUsername())
                .orElse("A user");
        notificationService.notifyBidReceived(auction.getUserId(), bidderName, request.getBidAmount(), auction.getTitle(), auctionId);
        userRepository.findById(auction.getUserId()).ifPresent(owner ->
            sendEmailSilently(() -> emailService.sendBidReceivedEmail(
                owner.getEmail(),
                auction.getTitle(),
                request.getBidAmount().toString(),
                "http://localhost:3000/auctions/" + auctionId
            ))
        );

        return mapToResponse(saved);
    }

    /**
     * Get all bids for an auction, ordered highest bid first.
     */
    public List<BidResponse> getBidsForAuction(UUID auctionId) {
        if (!auctionRepository.existsById(auctionId)) {
            throw new RuntimeException("Auction not found");
        }
        return bidRepository.findByAuctionIdOrderByBidAmountDesc(auctionId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all bids placed by a specific user, enriched with auction info.
     */
    public List<BidResponse> getMyBids(UUID userId) {
        return bidRepository.findByUserId(userId)
                .stream()
                .map(bid -> {
                    BidResponse response = mapToResponse(bid);
                    auctionRepository.findById(bid.getAuctionId()).ifPresent(auction -> {
                        response.setAuctionTitle(auction.getTitle());
                        response.setAuctionStatus(auction.getStatus());
                        response.setAuctionImageUrl(auction.getImageUrl());
                        response.setCurrentPrice(auction.getCurrentPrice());
                    });
                    return response;
                })
                .collect(Collectors.toList());
    }

    private BidResponse mapToResponse(Bid bid) {
        BidResponse response = new BidResponse();
        response.setId(bid.getId());
        response.setAuctionId(bid.getAuctionId());
        response.setUserId(bid.getUserId());
        response.setBidAmount(bid.getBidAmount());
        response.setCreatedAt(bid.getCreatedAt());
        return response;
    }

    /** Sends email without letting failures break bid placement */
    private void sendEmailSilently(Runnable emailTask) {
        try {
            emailTask.run();
        } catch (Exception e) {
            System.err.println("[EMAIL_ERROR] Failed to send email: " + e.getMessage());
        }
    }
}
