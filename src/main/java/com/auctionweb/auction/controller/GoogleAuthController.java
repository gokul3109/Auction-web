package com.auctionweb.auction.controller;

import com.auctionweb.auction.dto.GoogleAuthRequest;
import com.auctionweb.auction.dto.UserResponse;
import com.auctionweb.auction.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class GoogleAuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/google")
    public ResponseEntity<UserResponse> googleLogin(@Valid @RequestBody GoogleAuthRequest request) {
        return ResponseEntity.ok(userService.loginWithGoogle(request.getGoogleToken()));
    }
}
