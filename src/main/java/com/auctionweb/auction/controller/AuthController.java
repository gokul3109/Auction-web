package com.auctionweb.auction.controller;

import com.auctionweb.auction.dto.ForgotPasswordRequest;
import com.auctionweb.auction.dto.LoginRequest;
import com.auctionweb.auction.dto.RegisterRequest;
import com.auctionweb.auction.dto.ResetPasswordRequest;
import com.auctionweb.auction.dto.UserResponse;
import com.auctionweb.auction.service.PasswordResetService;
import com.auctionweb.auction.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    /**
     * Step 1: user submits their email.
     * Always returns 200 OK — never reveals whether the email exists.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.processForgotPassword(request.getEmail());
        } catch (Exception ignored) {
            // Swallow all errors so we don't leak info
        }
        return ResponseEntity.ok(Map.of(
            "message", "If that email is registered, you'll receive a reset link shortly."
        ));
    }

    /**
     * Step 2: user submits the token from the email + new password.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password updated successfully."));
    }
}
