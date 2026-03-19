package com.auctionweb.auction.controller;

import com.auctionweb.auction.dto.AuctionRequest;
import com.auctionweb.auction.dto.AuctionResponse;
import com.auctionweb.auction.service.AuctionEventService;
import com.auctionweb.auction.service.AuctionService;
import com.auctionweb.auction.service.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuctionEventService auctionEventService;

    @Autowired
    private com.auctionweb.auction.service.AuctionEndingService auctionEndingService;

    @PostMapping
    public ResponseEntity<AuctionResponse> createAuction(
            @Valid @RequestBody AuctionRequest request,
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(auctionService.createAuction(request, extractUserId(authHeader)));
    }

    @GetMapping
    public ResponseEntity<List<AuctionResponse>> getAllAuctions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(auctionService.getAllAuctions(status, category, extractUserIdOptional(authHeader)));
    }

    @GetMapping("/my")
    public ResponseEntity<List<AuctionResponse>> getMyAuctions(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(auctionService.getMyAuctions(extractUserId(authHeader)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuctionResponse> getAuction(
            @PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(auctionService.getAuctionById(id, extractUserIdOptional(authHeader)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuctionResponse> updateAuction(
            @PathVariable UUID id,
            @Valid @RequestBody AuctionRequest request,
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(auctionService.updateAuction(id, request, extractUserId(authHeader)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuction(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        auctionService.deleteAuction(id, extractUserId(authHeader));
        return ResponseEntity.noContent().build();
    }

    /**
     * Server-Sent Events (SSE) endpoint for real-time auction updates.
     * Clients connect to this endpoint to receive bid events for a specific auction.
     * 
     * Usage (JavaScript):
     *   const eventSource = new EventSource('/api/auctions/123/events');
     *   eventSource.addEventListener('bid', (event) => {
     *     const bid = JSON.parse(event.data);
     *     updateUI(bid);
     *   });
     */
    @GetMapping("/{id}/events")
    public SseEmitter subscribeToAuctionEvents(@PathVariable UUID id) {
        return auctionEventService.subscribe(id);
    }

    /**
     * GET /api/auctions/{id}/winner
     * Returns the winner of a completed auction.
     * 
     * Response (auction has bids):
     * {
     *   "auctionId": "...",
     *   "bidId": "...",
     *   "winnerId": "...",
     *   "winningBidAmount": 500.00,
     *   "winTime": "2026-03-08T10:30:00",
     *   "hasWinner": true
     * }
     * 
     * Response (auction has no bids):
     * {
     *   "auctionId": "...",
     *   "hasWinner": false
     * }
     * 
     * Returns null if auction is still active (no winner determined yet).
     */
    @GetMapping("/{id}/winner")
    public ResponseEntity<?> getWinner(@PathVariable UUID id) {
        com.auctionweb.auction.service.AuctionWinnerInfo winnerInfo = auctionEndingService.getWinnerInfo(id);
        if (winnerInfo == null) {
            return ResponseEntity.ok().body(java.util.Map.of(
                "message", "Auction is still active. No winner determined yet."
            ));
        }
        return ResponseEntity.ok(winnerInfo);
    }

    /**
     * GET /api/auctions/{id}/stats
     * Returns comprehensive statistics about an auction.
     * 
     * Response:
     * {
     *   "auctionId": "...",
     *   "totalBids": 5,
     *   "highestBid": 500.00,
     *   "lowestBid": 100.00,
     *   "averageBid": 300.00,
     *   "uniqueBidders": 4,
     *   "timeRemaining": 3600,  // in seconds, null if completed
     *   "isActive": true,
     *   "isCompleted": false,
     *   "winnerInfo": null      // or AuctionWinnerInfo object if completed
     * }
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<com.auctionweb.auction.service.AuctionStats> getAuctionStats(@PathVariable UUID id) {
        return ResponseEntity.ok(auctionEndingService.getAuctionStats(id));
    }

    /**
     * GET /api/auctions/{id}/time-remaining
     * Returns time remaining (in seconds) for an auction.
     * Null if auction has already ended.
     */
    @GetMapping("/{id}/time-remaining")
    public ResponseEntity<?> getTimeRemaining(@PathVariable UUID id) {
        Long secondsRemaining = auctionEndingService.getTimeRemaining(id);
        if (secondsRemaining == null) {
            return ResponseEntity.ok().body(java.util.Map.of(
                "message", "Auction has ended",
                "secondsRemaining", 0
            ));
        }
        return ResponseEntity.ok().body(java.util.Map.of(
            "secondsRemaining", secondsRemaining
        ));
    }

    private UUID extractUserId(String authHeader) {
        String token = authHeader.substring(7);
        return UUID.fromString(jwtUtil.extractUserId(token));
    }

    private UUID extractUserIdOptional(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            return null;
        }
        return UUID.fromString(jwtUtil.extractUserId(token));
    }
}
