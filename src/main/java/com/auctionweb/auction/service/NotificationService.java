package com.auctionweb.auction.service;

import com.auctionweb.auction.model.Notification;
import com.auctionweb.auction.model.Notification.Type;
import com.auctionweb.auction.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // ─── Fetch ───────────────────────────────────────────────────────────────

    public List<Notification> getMyNotifications(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    // ─── Mark read ───────────────────────────────────────────────────────────

    public void markRead(UUID notificationId, UUID userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getUserId().equals(userId)) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }

    public void markAllRead(UUID userId) {
        List<Notification> unread = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    // ─── Create helpers (called from BidService & AuctionEndingService) ──────

    /** Someone placed a higher bid — notify the previous highest bidder */
    public void notifyOutbid(UUID outbidUserId, String auctionTitle, UUID auctionId) {
        save(new Notification(
            outbidUserId,
            Type.OUTBID,
            "You've been outbid",
            "Someone placed a higher bid on \"" + auctionTitle + "\". Bid again to stay in the lead.",
            auctionId
        ));
    }

    /** Notify the auction owner that a new bid was placed */
    public void notifyBidReceived(UUID ownerId, String bidderUsername, BigDecimal amount, String auctionTitle, UUID auctionId) {
        save(new Notification(
            ownerId,
            Type.BID_RECEIVED,
            "New bid on your auction",
            bidderUsername + " placed a bid of $" + amount + " on \"" + auctionTitle + "\".",
            auctionId
        ));
    }

    /** Notify the winner when an auction ends */
    public void notifyAuctionWon(UUID winnerId, String auctionTitle, BigDecimal amount, UUID auctionId) {
        save(new Notification(
            winnerId,
            Type.AUCTION_WON,
            "You won an auction!",
            "Congratulations! You won \"" + auctionTitle + "\" for $" + amount + ".",
            auctionId
        ));
    }

    /** Notify the seller that their auction sold */
    public void notifyAuctionSold(UUID sellerId, String auctionTitle, BigDecimal amount, UUID auctionId) {
        save(new Notification(
            sellerId,
            Type.AUCTION_SOLD,
            "Your auction sold",
            "\"" + auctionTitle + "\" ended and sold for $" + amount + ".",
            auctionId
        ));
    }

    /** Notify the seller that their auction ended with no bids */
    public void notifyAuctionNoBids(UUID sellerId, String auctionTitle, UUID auctionId) {
        save(new Notification(
            sellerId,
            Type.AUCTION_NO_BIDS,
            "Auction ended with no bids",
            "\"" + auctionTitle + "\" ended but received no bids.",
            auctionId
        ));
    }

    private void save(Notification notification) {
        try {
            notificationRepository.save(notification);
        } catch (Exception e) {
            // Never let notification failure break the main flow
            System.err.println("[NOTIFICATION_ERROR] Failed to save notification: " + e.getMessage());
        }
    }
}
