package com.auctionweb.auction.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * Utility class for generating and validating JWT tokens.
 *
 * A JWT looks like:  header.payload.signature
 *   header    = algorithm info (base64 encoded)
 *   payload   = user data like email, userId, expiry (base64 encoded - NOT encrypted!)
 *   signature = HMAC_SHA256(header + payload, secretKey) - tamper proof
 */
@Component
public class JwtUtil {

    // Loaded from application.properties
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration; // milliseconds

    /**
     * Generate a JWT token for a logged-in user.
     * The token contains: email (subject), userId, issued time, expiry time.
     */
    public String generateToken(String email, String userId) {
        return Jwts.builder()
                .subject(email)                        // who this token is for
                .claim("userId", userId)               // extra data we store
                .issuedAt(new Date())                  // when it was created
                .expiration(new Date(System.currentTimeMillis() + expiration)) // when it expires
                .signWith(getSigningKey())             // sign with our secret key
                .compact();                            // build the final string
    }

    /**
     * Extract the email from a JWT token.
     */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extract the userId from a JWT token.
     */
    public String extractUserId(String token) {
        return parseClaims(token).get("userId", String.class);
    }

    /**
     * Check if a JWT token is valid (correct signature + not expired).
     */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token); // throws if invalid or expired
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ---- Private helpers ----

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())  // use our secret to verify signature
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
