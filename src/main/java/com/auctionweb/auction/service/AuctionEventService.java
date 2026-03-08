package com.auctionweb.auction.service;

import com.auctionweb.auction.dto.BidEventMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Manages Server-Sent Events (SSE) emitters for real-time auction updates.
 * Maintains a map of auction ID → list of connected clients
 * When a bid is placed, broadcasts to all clients watching that auction.
 */
@Service
public class AuctionEventService {

    // auctionId → List of SseEmitters (clients watching this auction)
    private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    // auctionId → Deque of bid history (last 100 bids per auction)
    private final Map<UUID, Deque<BidEventMessage>> bidHistory = new ConcurrentHashMap<>();

    // 3-minute timeout for SSE connections
    private static final long SSE_TIMEOUT = 180000L;

    // Heartbeat interval (30 seconds) to keep connection alive
    private static final long HEARTBEAT_INTERVAL = 30000L;

    // Max bids to store in history per auction
    private static final int MAX_BID_HISTORY = 100;

    @Autowired(required = false)
    private ScheduledExecutorService scheduler;

    @PostConstruct
    public void init() {
        if (scheduler == null) {
            scheduler = Executors.newScheduledThreadPool(1, r -> {
                Thread t = new Thread(r, "SSE-Heartbeat-Thread");
                t.setDaemon(true);
                return t;
            });
        }
    }

    /**
     * Register a new SSE client for an auction.
     * Client will receive events for this auction until connection is closed.
     * Also replays bid history so client catches up on missed bids.
     */
    public SseEmitter subscribe(UUID auctionId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // Add to the list of emitters for this auction
        emitters.computeIfAbsent(auctionId, k -> new CopyOnWriteArrayList<>())
                .add(emitter);

        // Replay bid history to client (so they catch up on missed bids)
        Deque<BidEventMessage> history = bidHistory.get(auctionId);
        if (history != null && !history.isEmpty()) {
            try {
                for (BidEventMessage bid : history) {
                    emitter.send(SseEmitter.event()
                            .id(UUID.randomUUID().toString())
                            .name("bid-history")
                            .data(bid, org.springframework.http.MediaType.APPLICATION_JSON)
                            .build()
                    );
                }
            } catch (IOException e) {
                // Could not send history
            }
        }

        // Schedule heartbeat to keep connection alive (prevents proxy disconnection)
        ScheduledFuture<?> heartbeatTask = scheduler.scheduleAtFixedRate(
                () -> sendHeartbeat(emitter),
                HEARTBEAT_INTERVAL,
                HEARTBEAT_INTERVAL,
                TimeUnit.MILLISECONDS
        );

        // Handle client disconnect
        emitter.onCompletion(() -> {
            heartbeatTask.cancel(false);
            removeEmitter(auctionId, emitter);
        });
        
        emitter.onTimeout(() -> {
            heartbeatTask.cancel(false);
            removeEmitter(auctionId, emitter);
        });

        return emitter;
    }

    /**
     * Send a heartbeat comment to keep connection alive.
     * Corporate proxies close idle connections after ~5 minutes.
     */
    private void sendHeartbeat(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .id(UUID.randomUUID().toString())
                    .name("heartbeat")
                    .comment("keeping connection alive")
                    .build()
            );
        } catch (IOException e) {
            // Connection closed, will be removed by onCompletion/onTimeout
        }
    }

    /**
     * Broadcast a bid event to all clients watching this auction.
     * Also stores bid in history for clients that reconnect later.
     * This is called whenever a new bid is placed.
     */
    public void broadcastBidEvent(UUID auctionId, BidEventMessage bidEvent) {
        // Store in history (for reconnecting clients)
        Deque<BidEventMessage> history = bidHistory.computeIfAbsent(auctionId, k -> new ConcurrentLinkedDeque<>());
        history.addLast(bidEvent);
        // Keep only last 100 bids to avoid memory bloat
        while (history.size() > MAX_BID_HISTORY) {
            history.removeFirst();
        }

        List<SseEmitter> auctionEmitters = emitters.get(auctionId);
        
        if (auctionEmitters == null || auctionEmitters.isEmpty()) {
            return; // No clients watching this auction
        }

        List<SseEmitter> failedEmitters = new ArrayList<>();

        for (SseEmitter emitter : auctionEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .id(UUID.randomUUID().toString())
                        .name("bid")
                        .data(bidEvent, org.springframework.http.MediaType.APPLICATION_JSON)
                        .build()
                );
            } catch (IOException e) {
                // Client disconnected or error sending
                failedEmitters.add(emitter);
            }
        }

        // Remove failed emitters
        failedEmitters.forEach(emitter -> removeEmitter(auctionId, emitter));
    }

    /**
     * Remove an emitter from the list (when client disconnects).
     */
    private void removeEmitter(UUID auctionId, SseEmitter emitter) {
        List<SseEmitter> auctionEmitters = emitters.get(auctionId);
        if (auctionEmitters != null) {
            auctionEmitters.remove(emitter);
            if (auctionEmitters.isEmpty()) {
                emitters.remove(auctionId);
            }
        }
    }

    /**
     * Get number of clients watching an auction (for monitoring).
     */
    public int getSubscriberCount(UUID auctionId) {
        List<SseEmitter> list = emitters.get(auctionId);
        return list != null ? list.size() : 0;
    }

    /**
     * Get total connected clients across all auctions.
     */
    public int getTotalSubscribers() {
        return emitters.values().stream()
                .mapToInt(List::size)
                .sum();
    }
}
