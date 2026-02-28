package com.auctionweb.auction.controller;

import com.auctionweb.auction.dto.AuctionRequest;
import com.auctionweb.auction.dto.AuctionResponse;
import com.auctionweb.auction.service.AuctionService;
import com.auctionweb.auction.service.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * POST /api/auctions
     * Create a new auction. Requires JWT.
     * userId is extracted from the token — not from the request body.
     */
    @PostMapping
    public ResponseEntity<?> createAuction(
            @RequestBody AuctionRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            UUID userId = extractUserId(authHeader);
            AuctionResponse response = auctionService.createAuction(request, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * GET /api/auctions
     * List all auctions. Optional filters: ?status=active  ?category=electronics
     */
    @GetMapping
    public ResponseEntity<?> getAllAuctions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {
        try {
            List<AuctionResponse> auctions = auctionService.getAllAuctions(status, category);
            return ResponseEntity.ok(auctions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * GET /api/auctions/{id}
     * Get a single auction by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAuction(@PathVariable UUID id) {
        try {
            AuctionResponse response = auctionService.getAuctionById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * PUT /api/auctions/{id}
     * Update an auction. Only the owner can do this.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAuction(
            @PathVariable UUID id,
            @RequestBody AuctionRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            UUID userId = extractUserId(authHeader);
            AuctionResponse response = auctionService.updateAuction(id, request, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * DELETE /api/auctions/{id}
     * Delete an auction. Only the owner can do this.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAuction(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            UUID userId = extractUserId(authHeader);
            auctionService.deleteAuction(id, userId);
            return ResponseEntity.ok("{\"message\": \"Auction deleted successfully\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // ---- Helper ----

    /**
     * Extract the userId UUID from the JWT token in the Authorization header.
     * Header format: "Bearer <token>"
     */
    private UUID extractUserId(String authHeader) {
        String token = authHeader.substring(7); // remove "Bearer "
        String userIdStr = jwtUtil.extractUserId(token);
        return UUID.fromString(userIdStr);
    }
}
