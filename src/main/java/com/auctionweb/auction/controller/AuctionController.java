package com.auctionweb.auction.controller;

import com.auctionweb.auction.dto.AuctionRequest;
import com.auctionweb.auction.dto.AuctionResponse;
import com.auctionweb.auction.service.AuctionService;
import com.auctionweb.auction.service.JwtUtil;
import jakarta.validation.Valid;
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

    @PostMapping
    public ResponseEntity<AuctionResponse> createAuction(
            @Valid @RequestBody AuctionRequest request,
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(auctionService.createAuction(request, extractUserId(authHeader)));
    }

    @GetMapping
    public ResponseEntity<List<AuctionResponse>> getAllAuctions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(auctionService.getAllAuctions(status, category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuctionResponse> getAuction(@PathVariable UUID id) {
        return ResponseEntity.ok(auctionService.getAuctionById(id));
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

    private UUID extractUserId(String authHeader) {
        String token = authHeader.substring(7);
        return UUID.fromString(jwtUtil.extractUserId(token));
    }
}
