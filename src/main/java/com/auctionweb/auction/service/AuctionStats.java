package com.auctionweb.auction.service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Comprehensive statistics about an auction.
 * Includes bid information, timing, and winner details.
 */
public class AuctionStats {
    private UUID auctionId;
    private Integer totalBids;
    private BigDecimal highestBid;
    private BigDecimal lowestBid;
    private BigDecimal averageBid;
    private Long uniqueBidders;
    private Long timeRemaining; // in seconds, null if completed
    private Boolean isActive;
    private Boolean isCompleted;
    private AuctionWinnerInfo winnerInfo;

    private AuctionStats(Builder builder) {
        this.auctionId = builder.auctionId;
        this.totalBids = builder.totalBids;
        this.highestBid = builder.highestBid;
        this.lowestBid = builder.lowestBid;
        this.averageBid = builder.averageBid;
        this.uniqueBidders = builder.uniqueBidders;
        this.timeRemaining = builder.timeRemaining;
        this.isActive = builder.isActive;
        this.isCompleted = builder.isCompleted;
        this.winnerInfo = builder.winnerInfo;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID auctionId;
        private Integer totalBids;
        private BigDecimal highestBid;
        private BigDecimal lowestBid;
        private BigDecimal averageBid;
        private Long uniqueBidders;
        private Long timeRemaining;
        private Boolean isActive;
        private Boolean isCompleted;
        private AuctionWinnerInfo winnerInfo;

        public Builder auctionId(UUID auctionId) { this.auctionId = auctionId; return this; }
        public Builder totalBids(Integer totalBids) { this.totalBids = totalBids; return this; }
        public Builder highestBid(BigDecimal highestBid) { this.highestBid = highestBid; return this; }
        public Builder lowestBid(BigDecimal lowestBid) { this.lowestBid = lowestBid; return this; }
        public Builder averageBid(BigDecimal averageBid) { this.averageBid = averageBid; return this; }
        public Builder uniqueBidders(Long uniqueBidders) { this.uniqueBidders = uniqueBidders; return this; }
        public Builder timeRemaining(Long timeRemaining) { this.timeRemaining = timeRemaining; return this; }
        public Builder isActive(Boolean isActive) { this.isActive = isActive; return this; }
        public Builder isCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; return this; }
        public Builder winnerInfo(AuctionWinnerInfo winnerInfo) { this.winnerInfo = winnerInfo; return this; }

        public AuctionStats build() {
            return new AuctionStats(this);
        }
    }

    // Getters
    public UUID getAuctionId() { return auctionId; }
    public Integer getTotalBids() { return totalBids; }
    public BigDecimal getHighestBid() { return highestBid; }
    public BigDecimal getLowestBid() { return lowestBid; }
    public BigDecimal getAverageBid() { return averageBid; }
    public Long getUniqueBidders() { return uniqueBidders; }
    public Long getTimeRemaining() { return timeRemaining; }
    public Boolean getIsActive() { return isActive; }
    public Boolean getIsCompleted() { return isCompleted; }
    public AuctionWinnerInfo getWinnerInfo() { return winnerInfo; }

    // Setters
    public void setAuctionId(UUID auctionId) { this.auctionId = auctionId; }
    public void setTotalBids(Integer totalBids) { this.totalBids = totalBids; }
    public void setHighestBid(BigDecimal highestBid) { this.highestBid = highestBid; }
    public void setLowestBid(BigDecimal lowestBid) { this.lowestBid = lowestBid; }
    public void setAverageBid(BigDecimal averageBid) { this.averageBid = averageBid; }
    public void setUniqueBidders(Long uniqueBidders) { this.uniqueBidders = uniqueBidders; }
    public void setTimeRemaining(Long timeRemaining) { this.timeRemaining = timeRemaining; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }
    public void setWinnerInfo(AuctionWinnerInfo winnerInfo) { this.winnerInfo = winnerInfo; }
}
