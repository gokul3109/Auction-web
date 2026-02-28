package com.auctionweb.auction.dto;

/**
 * Request body for POST /api/auth/google
 * The frontend sends the Google ID token it received from Google.
 */
public class GoogleAuthRequest {

    private String googleToken;

    public String getGoogleToken() { return googleToken; }
    public void setGoogleToken(String googleToken) { this.googleToken = googleToken; }
}
