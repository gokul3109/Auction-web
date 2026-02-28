package com.auctionweb.auction.repository;

import com.auctionweb.auction.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface BidRepository extends JpaRepository<Bid, UUID> {
    List<Bid> findByAuctionId(UUID auctionId);
    List<Bid> findByUserId(UUID userId);
    List<Bid> findByAuctionIdOrderByBidAmountDesc(UUID auctionId);
}
