package com.auctionweb.auction.controller;

import com.auctionweb.auction.dto.BidEventMessage;
import com.auctionweb.auction.dto.BidRequest;
import com.auctionweb.auction.dto.BidResponse;
import com.auctionweb.auction.service.AuctionEventService;
import com.auctionweb.auction.service.BidService;
import com.auctionweb.auction.service.JwtUtil;
import com.auctionweb.auction.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auctions")
public class BidController {

    @Autowired
    private BidService bidService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private AuctionEventService auctionEventService;

    @PostMapping("/{id}/bids")
    public ResponseEntity<BidResponse> placeBid(
            @PathVariable UUID id,
            @Valid @RequestBody BidRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        UUID userId = extractUserId(authHeader);
        BidResponse bidResponse = bidService.placeBid(id, request, userId);
        
        // Get bidder username for event message
        String bidderUsername = userService.getUserById(userId).getUsername();
        
        // Get total bid count for this auction
        List<BidResponse> allBids = bidService.getBidsForAuction(id);
        
        // Broadcast bid event to all clients watching this auction via SSE
        BidEventMessage event = new BidEventMessage(
            id,
            userId,
            bidderUsername,
            bidResponse.getBidAmount(),
            LocalDateTime.now(),
            allBids.size()
        );
        auctionEventService.broadcastBidEvent(id, event);
        
        return ResponseEntity.ok(bidResponse);
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
