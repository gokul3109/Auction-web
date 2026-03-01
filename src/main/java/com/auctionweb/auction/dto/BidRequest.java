package com.auctionweb.auction.dto;

import java.math.BigDecimal;

/**
 * Incoming JSON body when placing a bid.
 * POST /api/auctions/{id}/bids
 */
public class BidRequest {

    private BigDecimal bidAmount;

    public BigDecimal getBidAmount() { return bidAmount; }
    public void setBidAmount(BigDecimal bidAmount) { this.bidAmount = bidAmount; }
}
