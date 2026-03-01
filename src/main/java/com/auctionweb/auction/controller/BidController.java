package com.auctionweb.auction.controller;

import com.auctionweb.auction.dto.BidRequest;
import com.auctionweb.auction.dto.BidResponse;
import com.auctionweb.auction.service.BidService;
import com.auctionweb.auction.service.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auctions")
public class BidController {

    @Autowired
    private BidService bidService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * POST /api/auctions/{id}/bids
     * Place a bid on an auction. Requires JWT.
     */
    @PostMapping("/{id}/bids")
    public ResponseEntity<?> placeBid(
            @PathVariable UUID id,
            @RequestBody BidRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            UUID userId = extractUserId(authHeader);
            BidResponse response = bidService.placeBid(id, request, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * GET /api/auctions/{id}/bids
     * Get all bids for an auction, highest first. Public endpoint.
     */
    @GetMapping("/{id}/bids")
    public ResponseEntity<?> getBids(@PathVariable UUID id) {
        try {
            List<BidResponse> bids = bidService.getBidsForAuction(id);
            return ResponseEntity.ok(bids);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // ---- Helper ----

    private UUID extractUserId(String authHeader) {
        String token = authHeader.substring(7); // remove "Bearer "
        String userIdStr = jwtUtil.extractUserId(token);
        return UUID.fromString(userIdStr);
    }
}
