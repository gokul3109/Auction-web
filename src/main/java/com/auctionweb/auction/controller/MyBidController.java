package com.auctionweb.auction.controller;

import com.auctionweb.auction.dto.BidResponse;
import com.auctionweb.auction.service.BidService;
import com.auctionweb.auction.service.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bids")
public class MyBidController {

    @Autowired
    private BidService bidService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/my")
    public ResponseEntity<List<BidResponse>> getMyBids(
            @RequestHeader("Authorization") String authHeader) {
        UUID userId = UUID.fromString(jwtUtil.extractUserId(authHeader.substring(7)));
        return ResponseEntity.ok(bidService.getMyBids(userId));
    }
}
