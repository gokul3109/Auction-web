package com.auctionweb.auction.controller;

import com.auctionweb.auction.dto.BidRequest;
import com.auctionweb.auction.dto.BidResponse;
import com.auctionweb.auction.service.BidService;
import com.auctionweb.auction.service.JwtUtil;
import jakarta.validation.Valid;
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

    @PostMapping("/{id}/bids")
    public ResponseEntity<BidResponse> placeBid(
            @PathVariable UUID id,
            @Valid @RequestBody BidRequest request,
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(bidService.placeBid(id, request, extractUserId(authHeader)));
    }

    @GetMapping("/{id}/bids")
    public ResponseEntity<List<BidResponse>> getBids(@PathVariable UUID id) {
        return ResponseEntity.ok(bidService.getBidsForAuction(id));
    }

    private UUID extractUserId(String authHeader) {
        String token = authHeader.substring(7);
        return UUID.fromString(jwtUtil.extractUserId(token));
    }
}
