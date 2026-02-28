package com.auctionweb.auction.repository;

import com.auctionweb.auction.model.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, UUID> {
    List<Auction> findByUserId(UUID userId);
    List<Auction> findByStatus(String status);
    List<Auction> findByCategory(String category);
}
