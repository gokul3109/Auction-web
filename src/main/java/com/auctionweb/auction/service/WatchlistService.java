package com.auctionweb.auction.service;

import com.auctionweb.auction.dto.AuctionResponse;
import com.auctionweb.auction.model.Auction;
import com.auctionweb.auction.model.Watchlist;
import com.auctionweb.auction.repository.AuctionRepository;
import com.auctionweb.auction.repository.WatchlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WatchlistService {

    @Autowired
    private WatchlistRepository watchlistRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private AuctionService auctionService;

    public void addToWatchlist(UUID userId, UUID auctionId) {
        if (!auctionRepository.existsById(auctionId)) {
            throw new RuntimeException("Auction not found");
        }

        if (watchlistRepository.existsByUserIdAndAuctionId(userId, auctionId)) {
            return;
        }

        watchlistRepository.save(new Watchlist(userId, auctionId));
    }

    public void removeFromWatchlist(UUID userId, UUID auctionId) {
        watchlistRepository.deleteByUserIdAndAuctionId(userId, auctionId);
    }

    public List<AuctionResponse> getMyWatchlist(UUID userId) {
        List<UUID> auctionIds = watchlistRepository.findByUserId(userId)
                .stream()
                .map(Watchlist::getAuctionId)
                .toList();

        if (auctionIds.isEmpty()) {
            return List.of();
        }

        return auctionRepository.findAllById(auctionIds)
                .stream()
                .map(auction -> auctionService.mapToResponse(auction, true))
                .collect(Collectors.toList());
    }
}
