package com.auctionweb.auction.repository;

import com.auctionweb.auction.model.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, UUID> {
    List<Auction> findByUserId(UUID userId);
    List<Auction> findByStatus(String status);
    List<Auction> findByCategory(String category);
    List<Auction> findByStatusAndCategory(String status, String category);
    
    // Find active auctions where endDate has passed (for auto-ending)
    List<Auction> findByStatusAndEndDateBefore(String status, LocalDateTime now);
}
