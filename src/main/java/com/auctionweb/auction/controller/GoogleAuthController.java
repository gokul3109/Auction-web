package com.auctionweb.auction.controller;

import com.auctionweb.auction.dto.GoogleAuthRequest;
import com.auctionweb.auction.dto.UserResponse;
import com.auctionweb.auction.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles Google OAuth login.
 *
 * Flow:
 *   1. Frontend gets a Google ID token from Google (after user clicks "Login with Google")
 *   2. Frontend sends that token here: POST /api/auth/google
 *   3. This controller passes it to UserService.loginWithGoogle()
 *   4. We verify it with Google, find/create the user, return OUR JWT
 */
@RestController
@RequestMapping("/api/auth")
public class GoogleAuthController {

    @Autowired
    private UserService userService;

    /**
     * POST /api/auth/google
     * Body: { "googleToken": "<token from Google>" }
     * Returns: UserResponse with our JWT token
     */
    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleAuthRequest request) {
        try {
            UserResponse response = userService.loginWithGoogle(request.getGoogleToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
