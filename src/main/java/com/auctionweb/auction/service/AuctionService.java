package com.auctionweb.auction.service;

import com.auctionweb.auction.dto.AuctionRequest;
import com.auctionweb.auction.dto.AuctionResponse;
import com.auctionweb.auction.model.Auction;
import com.auctionweb.auction.repository.AuctionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuctionService {

    @Autowired
    private AuctionRepository auctionRepository;

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
        return mapToResponse(saved);
    }

    /**
     * Get all auctions. Optionally filter by status or category.
     * Examples:
     *   GET /api/auctions                      → all auctions
     *   GET /api/auctions?status=active        → only active auctions
     *   GET /api/auctions?category=electronics → only electronics
     */
    public List<AuctionResponse> getAllAuctions(String status, String category) {
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

        return auctions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all auctions belonging to the authenticated user.
     */
    public List<AuctionResponse> getMyAuctions(UUID userId) {
        return auctionRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a single auction by ID.
     * If auction has expired, it will be marked as completed immediately.
     */
    public AuctionResponse getAuctionById(UUID id) {
        // Check if expired and end immediately (don't wait for scheduler)
        auctionEndingService.ensureAuctionNotExpired(id);
        
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Auction not found"));
        return mapToResponse(auction);
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
        return mapToResponse(updated);
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
    private AuctionResponse mapToResponse(Auction auction) {
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
        response.setCreatedAt(auction.getCreatedAt());
        response.setUpdatedAt(auction.getUpdatedAt());
        return response;
    }
}
