package com.auctionweb.auction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event message sent to clients via SSE when a bid is placed.
 * This is broadcast to all clients watching the same auction.
 */
public class BidEventMessage {

    private UUID auctionId;
    private UUID bidderId;
    private String bidderUsername;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private Integer totalBids;

    public BidEventMessage() {}

    public BidEventMessage(UUID auctionId, UUID bidderId, String bidderUsername, 
                          BigDecimal amount, LocalDateTime timestamp, Integer totalBids) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidderUsername = bidderUsername;
        this.amount = amount;
        this.timestamp = timestamp;
        this.totalBids = totalBids;
    }

    public UUID getAuctionId() { return auctionId; }
    public void setAuctionId(UUID auctionId) { this.auctionId = auctionId; }

    public UUID getBidderId() { return bidderId; }
    public void setBidderId(UUID bidderId) { this.bidderId = bidderId; }

    public String getBidderUsername() { return bidderUsername; }
    public void setBidderUsername(String bidderUsername) { this.bidderUsername = bidderUsername; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Integer getTotalBids() { return totalBids; }
    public void setTotalBids(Integer totalBids) { this.totalBids = totalBids; }
}
