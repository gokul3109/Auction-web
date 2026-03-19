package com.auctionweb.auction.repository;

import com.auctionweb.auction.model.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, UUID> {
    List<Watchlist> findByUserId(UUID userId);
    boolean existsByUserIdAndAuctionId(UUID userId, UUID auctionId);

    @Transactional
    void deleteByUserIdAndAuctionId(UUID userId, UUID auctionId);
}
