package com.auctionweb.auction.controller;

import com.auctionweb.auction.model.Notification;
import com.auctionweb.auction.service.JwtUtil;
import com.auctionweb.auction.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtUtil jwtUtil;

    /** GET /api/notifications — all notifications for current user */
    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(notificationService.getMyNotifications(extractUserId(authHeader)));
    }

    /** GET /api/notifications/unread-count */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestHeader("Authorization") String authHeader) {
        long count = notificationService.getUnreadCount(extractUserId(authHeader));
        return ResponseEntity.ok(Map.of("count", count));
    }

    /** PATCH /api/notifications/{id}/read */
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        notificationService.markRead(id, extractUserId(authHeader));
        return ResponseEntity.noContent().build();
    }

    /** PATCH /api/notifications/read-all */
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(
            @RequestHeader("Authorization") String authHeader) {
        notificationService.markAllRead(extractUserId(authHeader));
        return ResponseEntity.noContent().build();
    }

    private UUID extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return UUID.fromString(jwtUtil.extractUserId(token));
    }
}
