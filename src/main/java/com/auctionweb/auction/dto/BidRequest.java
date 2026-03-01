package com.auctionweb.auction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Incoming JSON body when placing a bid.
 * POST /api/auctions/{id}/bids
 */
public class BidRequest {

    @NotNull(message = "Bid amount is required")
    @Positive(message = "Bid amount must be a positive number")
    private BigDecimal bidAmount;

    public BigDecimal getBidAmount() { return bidAmount; }
    public void setBidAmount(BigDecimal bidAmount) { this.bidAmount = bidAmount; }
}
