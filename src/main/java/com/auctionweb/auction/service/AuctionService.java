package com.auctionweb.auction.service;

import com.auctionweb.auction.dto.AuctionRequest;
import com.auctionweb.auction.dto.AuctionResponse;
import com.auctionweb.auction.model.Auction;
import com.auctionweb.auction.repository.AuctionRepository;
import com.auctionweb.auction.repository.WatchlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuctionService {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private WatchlistRepository watchlistRepository;

    @Autowired
    private com.auctionweb.auction.service.AuctionEndingService auctionEndingService;

    /**
     * Create a new auction.
     * The userId comes from the JWT token (set by JwtAuthFilter) — not from the request body.
     * This ensures users can only create auctions for themselves.
     */
    public AuctionResponse createAuction(AuctionRequest request, UUID userId) {
        Auction auction = new Auction();
        auction.setUserId(userId);
        auction.setTitle(request.getTitle());
        auction.setDescription(request.getDescription());
        auction.setStartingPrice(request.getStartingPrice());
        auction.setCurrentPrice(request.getStartingPrice()); // current price starts at starting price
        auction.setCategory(request.getCategory());
        auction.setStatus("active");
        auction.setStartDate(request.getStartDate());
        auction.setEndDate(request.getEndDate());
        auction.setImageUrl(request.getImageUrl());

        Auction saved = auctionRepository.save(auction);
        return mapToResponse(saved, false);
    }

    /**
     * Get all auctions. Optionally filter by status or category.
     * Examples:
     *   GET /api/auctions                      → all auctions
     *   GET /api/auctions?status=active        → only active auctions
     *   GET /api/auctions?category=electronics → only electronics
     */
    public List<AuctionResponse> getAllAuctions(String status, String category, UUID currentUserId) {
        List<Auction> auctions;

        if (status != null && category != null) {
            auctions = auctionRepository.findByStatusAndCategory(status, category);
        } else if (status != null) {
            auctions = auctionRepository.findByStatus(status);
        } else if (category != null) {
            auctions = auctionRepository.findByCategory(category);
        } else {
            auctions = auctionRepository.findAll();
        }

        Set<UUID> watchlistedAuctionIds = getWatchlistedAuctionIds(currentUserId);

        return auctions.stream()
            .map(auction -> mapToResponse(auction, watchlistedAuctionIds.contains(auction.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Get all auctions belonging to the authenticated user.
     */
    public List<AuctionResponse> getMyAuctions(UUID userId) {
        Set<UUID> watchlistedAuctionIds = getWatchlistedAuctionIds(userId);

        return auctionRepository.findByUserId(userId)
                .stream()
            .map(auction -> mapToResponse(auction, watchlistedAuctionIds.contains(auction.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Get a single auction by ID.
     * If auction has expired, it will be marked as completed immediately.
     */
    public AuctionResponse getAuctionById(UUID id, UUID currentUserId) {
        // Check if expired and end immediately (don't wait for scheduler)
        auctionEndingService.ensureAuctionNotExpired(id);
        
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Auction not found"));
        boolean isWatchlisted = currentUserId != null
                && watchlistRepository.existsByUserIdAndAuctionId(currentUserId, auction.getId());

        return mapToResponse(auction, isWatchlisted);
    }

    /**
     * Update an auction.
     * Only the owner (userId from JWT) can update their own auction.
     */
    public AuctionResponse updateAuction(UUID id, AuctionRequest request, UUID userId) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        // Check ownership
        if (!auction.getUserId().equals(userId)) {
            throw new RuntimeException("You are not allowed to update this auction");
        }

        auction.setTitle(request.getTitle());
        auction.setDescription(request.getDescription());
        auction.setCategory(request.getCategory());
        auction.setStartDate(request.getStartDate());
        auction.setEndDate(request.getEndDate());
        auction.setImageUrl(request.getImageUrl());
        // Note: startingPrice and currentPrice are NOT updated once bids may exist

        Auction updated = auctionRepository.save(auction);
        boolean isWatchlisted = watchlistRepository.existsByUserIdAndAuctionId(userId, updated.getId());
        return mapToResponse(updated, isWatchlisted);
    }

    /**
     * Delete an auction.
     * Only the owner (userId from JWT) can delete their own auction.
     */
    public void deleteAuction(UUID id, UUID userId) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        // Check ownership
        if (!auction.getUserId().equals(userId)) {
            throw new RuntimeException("You are not allowed to delete this auction");
        }

        auctionRepository.delete(auction);
    }

    /**
     * Convert Auction entity → AuctionResponse DTO
     */
    public AuctionResponse mapToResponse(Auction auction, boolean isWatchlisted) {
        AuctionResponse response = new AuctionResponse();
        response.setId(auction.getId());
        response.setUserId(auction.getUserId());
        response.setTitle(auction.getTitle());
        response.setDescription(auction.getDescription());
        response.setStartingPrice(auction.getStartingPrice());
        response.setCurrentPrice(auction.getCurrentPrice());
        response.setCategory(auction.getCategory());
        response.setStatus(auction.getStatus());
        response.setStartDate(auction.getStartDate());
        response.setEndDate(auction.getEndDate());
        response.setImageUrl(auction.getImageUrl());
        response.setWatchlisted(isWatchlisted);
        response.setCreatedAt(auction.getCreatedAt());
        response.setUpdatedAt(auction.getUpdatedAt());
        return response;
    }

    private Set<UUID> getWatchlistedAuctionIds(UUID currentUserId) {
        if (currentUserId == null) {
            return Set.of();
        }

        return watchlistRepository.findByUserId(currentUserId)
                .stream()
                .map(w -> w.getAuctionId())
                .collect(Collectors.toCollection(HashSet::new));
    }
}
