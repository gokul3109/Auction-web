package com.auctionweb.auction.controller;

import com.auctionweb.auction.dto.AuctionResponse;
import com.auctionweb.auction.service.JwtUtil;
import com.auctionweb.auction.service.WatchlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    @Autowired
    private WatchlistService watchlistService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/{auctionId}")
    public ResponseEntity<Map<String, String>> addToWatchlist(
            @PathVariable UUID auctionId,
            @RequestHeader("Authorization") String authHeader) {
        watchlistService.addToWatchlist(extractUserId(authHeader), auctionId);
        return ResponseEntity.ok(Map.of("message", "Added to watchlist"));
    }

    @DeleteMapping("/{auctionId}")
    public ResponseEntity<Void> removeFromWatchlist(
            @PathVariable UUID auctionId,
            @RequestHeader("Authorization") String authHeader) {
        watchlistService.removeFromWatchlist(extractUserId(authHeader), auctionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<AuctionResponse>> getMyWatchlist(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(watchlistService.getMyWatchlist(extractUserId(authHeader)));
    }

    private UUID extractUserId(String authHeader) {
        String token = authHeader.substring(7);
        return UUID.fromString(jwtUtil.extractUserId(token));
    }
}
