package com.auctionweb.auction.controller;

import com.auctionweb.auction.dto.UpdateProfileRequest;
import com.auctionweb.auction.dto.UserResponse;
import com.auctionweb.auction.service.JwtUtil;
import com.auctionweb.auction.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * GET /api/users/me
     * Returns the profile of the currently authenticated user.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(userService.getProfile(extractUserId(authHeader)));
    }

    /**
     * PUT /api/users/me
     * Updates username, fullName, and/or password for the authenticated user.
     * All fields are optional — only provided fields are changed.
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(userService.updateProfile(extractUserId(authHeader), request));
    }

    private UUID extractUserId(String authHeader) {
        String token = authHeader.substring(7);
        return UUID.fromString(jwtUtil.extractUserId(token));
    }
}
