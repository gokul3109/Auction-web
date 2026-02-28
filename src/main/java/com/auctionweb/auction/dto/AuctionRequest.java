package com.auctionweb.auction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Shape of the JSON body when creating or updating an auction.
 * POST /api/auctions  or  PUT /api/auctions/{id}
 */
public class AuctionRequest {

    private String title;
    private String description;
    private BigDecimal startingPrice;
    private String category;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String imageUrl;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getStartingPrice() { return startingPrice; }
    public void setStartingPrice(BigDecimal startingPrice) { this.startingPrice = startingPrice; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
