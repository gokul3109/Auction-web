package com.auctionweb.auction.service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Information about the winner of an auction.
 */
public class AuctionWinnerInfo {
    private UUID auctionId;
    private UUID bidId;
    private UUID winnerId;
    private java.math.BigDecimal winningBidAmount;
    private LocalDateTime winTime;
    private boolean hasWinner;

    public AuctionWinnerInfo() {}

    private AuctionWinnerInfo(UUID auctionId, UUID bidId, UUID winnerId, 
                             java.math.BigDecimal amount, LocalDateTime winTime) {
        this.auctionId = auctionId;
        this.bidId = bidId;
        this.winnerId = winnerId;
        this.winningBidAmount = amount;
        this.winTime = winTime;
        this.hasWinner = true;
    }

    private AuctionWinnerInfo(UUID auctionId, boolean noWinner) {
        this.auctionId = auctionId;
        this.hasWinner = false;
    }

    public static AuctionWinnerInfo withWinner(UUID auctionId, UUID bidId, UUID winnerId, 
                                               java.math.BigDecimal amount, LocalDateTime winTime) {
        return new AuctionWinnerInfo(auctionId, bidId, winnerId, amount, winTime);
    }

    public static AuctionWinnerInfo noWinner(UUID auctionId) {
        return new AuctionWinnerInfo(auctionId, false);
    }

    // Getters
    public UUID getAuctionId() { return auctionId; }
    public UUID getBidId() { return bidId; }
    public UUID getWinnerId() { return winnerId; }
    public java.math.BigDecimal getWinningBidAmount() { return winningBidAmount; }
    public LocalDateTime getWinTime() { return winTime; }
    public boolean isHasWinner() { return hasWinner; }

    // Setters
    public void setAuctionId(UUID auctionId) { this.auctionId = auctionId; }
    public void setBidId(UUID bidId) { this.bidId = bidId; }
    public void setWinnerId(UUID winnerId) { this.winnerId = winnerId; }
    public void setWinningBidAmount(java.math.BigDecimal amount) { this.winningBidAmount = amount; }
    public void setWinTime(LocalDateTime winTime) { this.winTime = winTime; }
    public void setHasWinner(boolean hasWinner) { this.hasWinner = hasWinner; }
}
