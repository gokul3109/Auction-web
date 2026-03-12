package com.auctionweb.auction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Outgoing JSON shape for a bid.
 * The auction* fields are populated only for the "my bids" endpoint.
 */
public class BidResponse {

    private UUID id;
    private UUID auctionId;
    private UUID userId;
    private BigDecimal bidAmount;
    private LocalDateTime createdAt;

    // Enriched fields — populated by BidService.getMyBids()
    private String auctionTitle;
    private String auctionStatus;
    private String auctionImageUrl;
    private BigDecimal currentPrice;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getAuctionId() { return auctionId; }
    public void setAuctionId(UUID auctionId) { this.auctionId = auctionId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public BigDecimal getBidAmount() { return bidAmount; }
    public void setBidAmount(BigDecimal bidAmount) { this.bidAmount = bidAmount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getAuctionTitle() { return auctionTitle; }
    public void setAuctionTitle(String auctionTitle) { this.auctionTitle = auctionTitle; }

    public String getAuctionStatus() { return auctionStatus; }
    public void setAuctionStatus(String auctionStatus) { this.auctionStatus = auctionStatus; }

    public String getAuctionImageUrl() { return auctionImageUrl; }
    public void setAuctionImageUrl(String auctionImageUrl) { this.auctionImageUrl = auctionImageUrl; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
}
