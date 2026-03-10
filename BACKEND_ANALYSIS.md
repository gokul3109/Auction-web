# Backend Analysis - Auction Web System

## ✅ CURRENTLY IMPLEMENTED

### Authentication & Users
- [x] Email/password registration
- [x] Email/password login
- [x] Google OAuth authentication
- [x] JWT token generation and validation
- [x] User profile fetch (`GET /api/users/me`)
- [x] User profile update (`PUT /api/users/me`)
- [x] Password change support

### Auctions
- [x] Create auction (`POST /api/auctions`)
- [x] Get all auctions (`GET /api/auctions`)
- [x] Filter auctions by status (`GET /api/auctions?status=active`)
- [x] Filter auctions by category (`GET /api/auctions?category=electronics`)
- [x] Get single auction (`GET /api/auctions/{id}`)
- [x] Update auction (`PUT /api/auctions/{id}`)
- [x] Delete auction (`DELETE /api/auctions/{id}`)
- [x] Ownership validation (only owner can update/delete)

### Bidding
- [x] Place bid (`POST /api/auctions/{id}/bids`)
- [x] Get all bids for auction (`GET /api/auctions/{id}/bids`)
- [x] Bid validation (higher than current price, not on own auction, active auction)
- [x] Update current price on bid

### Real-Time Updates (SSE)
- [x] SSE endpoint (`GET /api/auctions/{id}/events`)
- [x] Real-time bid broadcast
- [x] Automatic heartbeat (30 seconds)
- [x] Bid history buffer (last 100 bids)
- [x] Multiple concurrent client support

---

## ❌ MISSING - CRITICAL FEATURES

### 1. **Auction Lifecycle Management** (CRITICAL)
**Problem:** Auctions never end. Status is hardcoded to "active" forever.

**What's needed:**
```
- Scheduled job to end auctions when endDate passes
- Change status from "active" → "completed"/closed"
- Determine winner (highest bidder)
- Prevent bids on ended auctions
- Send winner notification
```

**Files to create:**
- `AuctionSchedulerService.java` - Uses `@Scheduled` to end auctions
- `AuctionWinnerService.java` - Determines and notifies winners

**Endpoints to add:**
- `GET /api/auctions/{id}/winner` - Get winning bid details

---

### 2. **User-Specific Auction Views** 
**Problem:** No way to see:
- My created auctions
- Auctions I've bid on
- My winning auctions

**Endpoints to add:**
```
GET /api/users/me/auctions           → My listed auctions
GET /api/users/me/bids               → Auctions I've bid on
GET /api/users/me/winning            → My won auctions
GET /api/users/{id}/auctions         → Public: see user's auctions
GET /api/users/{id}/ratings          → User's ratings/reviews
```

---

### 3. **Search & Discovery** 
**Problem:** Limited to category/status filtering. No real search.

**What's needed:**
```
- Full-text search by title/description
- Price range filtering (minPrice, maxPrice)
- Sort options (newest, ending soon, highest bids, lowest price)
- Pagination (limit, offset or page, size)
- Combined filters
```

**Endpoints to add:**
```
GET /api/auctions?search=laptop&minPrice=100&maxPrice=1000&sort=ending_soon&limit=20&offset=0
```

**Service to create:**
- `AuctionSearchService.java` - With advanced filtering logic

---

### 4. **Bid History & Details**
**Problem:** Bidding info is minimal:
- Can't see who placed each bid
- No detail about bidders
- Missing bid timestamps

**Model updates:**
```
Bid model needs:
- bidderUsername (denormalized for performance)
- bidderAvatarUrl
- timestamp is there ✓
```

**Endpoints to add:**
```
GET /api/auctions/{id}/bids/detailed → Shows bidder info
GET /api/auctions/{id}/top-bids      → Top 10 bids
```

---

### 5. **User Ratings & Reviews**
**Problem:** No way to rate sellers/buyers after transaction.

**Models to create:**
```
Rating.java
├── id (UUID)
├── auctionId (UUID) - link to the auction
├── fromUserId (UUID) - who left the rating
├── toUserId (UUID) - who gets rated
├── score (1-5)
├── comment (text)
├── createdAt
```

**Endpoints to add:**
```
POST /api/auctions/{id}/ratings     → Leave a rating
GET /api/users/{id}/ratings         → See user ratings
GET /api/auctions/{id}/bids/{bidId}/rating → Get rating for specific bid
```

---

### 6. **Watchlist / Followed Auctions**
**Problem:** Users can't save auctions to view later or get notified.

**Models to create:**
```
Watchlist.java
├── id (UUID)
├── userId (UUID)
├── auctionId (UUID)
├── createdAt
├── Unique constraint: (userId, auctionId)
```

**Endpoints to add:**
```
POST /api/users/me/watchlist/{auctionId}     → Add to watchlist
DELETE /api/users/me/watchlist/{auctionId}   → Remove from watchlist
GET /api/users/me/watchlist                  → Get my watchlist
GET /api/users/me/watchlist/check/{auctionId} → Is it in my watchlist?
```

---

### 7. **Notifications System**
**Problem:** Users don't get notified about:
- Outbid alerts
- Auction ending soon
- Auction ended / I won / I lost

**Models to create:**
```
Notification.java
├── id (UUID)
├── userId (UUID) - recipient
├── type (OUTBID, AUCTION_ENDING, AUCTION_ENDED, YOU_WON)
├── auctionId (UUID)
├── message
├── isRead (boolean)
├── createdAt
```

**Services to create:**
```
NotificationService.java
├── createNotification() - Create notification
├── markAsRead() - Mark notification as read
├── getUnreadCount() - Get unread count
```

**Endpoints to add:**
```
GET /api/notifications              → Get my notifications
PUT /api/notifications/{id}/read    → Mark as read
GET /api/notifications/unread-count → Get unread count
POST /api/notifications/subscribe   → SSE endpoint for real-time not
```

---

### 8. **Auction Statistics & Analytics**
**Problem:** No insights into auctions.

**Endpoints to add:**
```
GET /api/auctions/{id}/stats
Response:
{
  "totalBids": 5,
  "highestBid": 500,
  "lowestBid": 100,
  "averageBid": 300,
  "uniqueBidders": 4,
  "timeRemaining": "2 hours",
  "lastBidTime": "2026-03-08T10:30:00",
  "biddingActivity": [
    {"bidCount": 2, "hour": 10},
    {"bidCount": 1, "hour": 11}
  ]
}
```

---

### 9. **Comments / Q&A on Auctions**
**Problem:** Buyers can't ask seller questions.

**Models to create:**
```
Comment.java
├── id (UUID)
├── auctionId (UUID)
├── userId (UUID) - author
├── parentCommentId (UUID) - for replies
├── content (text)
├── createdAt
├── updatedAt
```

**Endpoints to add:**
```
POST /api/auctions/{id}/comments           → Post comment
GET /api/auctions/{id}/comments            → Get all comments
PUT /api/comments/{id}                     → Edit own comment
DELETE /api/comments/{id}                  → Delete own comment
POST /api/comments/{id}/replies            → Reply to comment
```

---

### 10. **Image Upload**
**Problem:** `imageUrl` is a string, but no upload endpoint.

**Services to create:**
```
ImageUploadService.java
├── uploadImage(file, auctionId) → Upload to cloud storage (S3/GCS)
├── deleteImage(imageUrl) → Clean up old images
```

**Endpoints to add:**
```
POST /api/auctions/{id}/images              → Upload image
DELETE /api/auctions/{id}/images/{imageId}  → Delete image
```

---

### 11. **Payment / Transactions**
**Problem:** No payment processing.

**Models to create:**
```
Transaction.java
├── id (UUID)
├── auctionId (UUID)
├── buyerId (UUID) - winner
├── sellerId (UUID) - auctioneer
├── amount (BigDecimal)
├── status (PENDING, PAID, FAILED, REFUNDED)
├── stripePaymentId (track Stripe)
├── createdAt

Payment should happen AFTER auction ends.
```

**Services to create:**
```
PaymentService.java (Stripe integration)
├── createPaymentIntent() - Create Stripe payment
├── confirmPayment() - Confirm payment
├── refund() - Refund if needed

TransactionService.java
├── createTransaction() - Record in DB
├── getTransaction()
├── updateStatus()
```

---

### 12. **Admin Features**
**Problem:** No admin endpoints.

**Endpoints to add:**
```
GET /api/admin/users                    → List all users
GET /api/admin/users/{id}               → Get user details
PUT /api/admin/users/{id}               → Edit user
DELETE /api/admin/users/{id}            → Delete user
GET /api/admin/users/{id}/suspend       → Suspend user

GET /api/admin/auctions                 → List all auctions
DELETE /api/admin/auctions/{id}         → Force delete auction
PUT /api/admin/auctions/{id}/status     → Change auction status (override)

GET /api/admin/reports                  → Flagged auctions/users
POST /api/admin/reports                 → Report auction/user
```

**Service to create:**
```
AdminService.java
├── validateIsAdmin() - Check if user is admin
├── suspendUser()
├── deleteUser()
├── forceDeleteAuction()
```

---

### 13. **Reporting / Moderation**
**Problem:** No way to report fraudulent auctions or users.

**Models to create:**
```
Report.java
├── id (UUID)
├── type (AUCTION, USER, BID)
├── targetId (UUID) - auction/user/bid ID
├── reporterId (UUID)
├── reason (text)
├── status (OPEN, REVIEWING, RESOLVED, DISMISSED)
├── adminNotes (text)
├── createdAt
├── resolvedAt
```

**Endpoints to add:**
```
POST /api/reports                  → Submit report
GET /api/admin/reports             → View all reports
PUT /api/admin/reports/{id}        → Update report status
```

---

### 14. **Error Handling & Validation**
**Problem:** Generic RuntimeExceptions, missing field validation.

**What's needed:**
```
- Custom exception classes for different errors
- Global exception handler (already exists but needs improvement)
- Validation constraints on DTOs
- Proper HTTP status codes
- Structured error responses
```

**Files to create/improve:**
```
GlobalExceptionHandler.java (improve existing)
├── Handle all custom exceptions
├── Return proper status codes (400, 403, 404, 409, etc.)

Custom Exceptions:
├── AuctionNotFoundException.java
├── UnauthorizedException.java
├── InvalidBidException.java
├── AuctionAlreadyEndedException.java
```

---

### 15. **Pagination & Limits**
**Problem:** List endpoints return ALL records.

**DTOs to create:**
```
PagedResponse.java (generic)
├── content (List<T>)
├── totalElements
├── totalPages
├── currentPage
├── hasNext
├── hasPrevious
```

**Update ALL list endpoints:**
```
GET /api/auctions?page=0&pageSize=20&sort=createdAt,desc
GET /api/auctions/{id}/bids?page=0&pageSize=10
GET /api/users/me/auctions?page=0&pageSize=20
```

---

## 📊 PRIORITY ROADMAP

### Phase 1 (MVP - CRITICAL)
1. ✅ Real-time bidding (SSE) - DONE
2. 🔴 **Auction ending/expiration logic**
3. 🔴 **Winner determination**
4. 🔴 **Pagination for list endpoints**
5. 🔴 **Improved error handling**

### Phase 2 (Core Features)
6. **User auctions & bid history**
7. **Search & filtering**
8. **Notifications system**
9. **Ratings & reviews**
10. **Watchlist**

### Phase 3 (Enhancement)
11. **Comments/Q&A**
12. **Image upload**
13. **Auction statistics**
14. **Admin features**

### Phase 4 (Production)
15. **Payment processing**
16. **Reporting/moderation**
17. **Rate limiting**
18. **Analytics**

---

## 🚀 NEXT IMMEDIATE STEP

**Build Auction Ending Service** because:
- Without it, auctions never end
- Can't determine winners
- Can't process payments
- Core lifecycle is incomplete

```java
// AuctionEndingService.java
@Service
public class AuctionEndingService {
    
    @Scheduled(fixedDelay = 60000) // Every minute
    public void checkAndEndExpiredAuctions() {
        // Find auctions where endDate < now
        // Change status to "completed"
        // Determine winner (highest bid)
        // Send notification to bidders
        // Create transaction record for payment
    }
    
    public List<BidResponse> getWinnerBid(UUID auctionId) {
        // Get highest bid for auction
    }
}
```

---

## Summary Table

| Feature | Status | Priority | Est. Time |
|---------|--------|----------|-----------|
| Authentication | ✅ Complete | - | - |
| Auctions CRUD | ✅ Complete | - | - |
| Bidding | ✅ Complete | - | - |
| Real-Time SSE | ✅ Complete | - | - |
| **Auction Ending** | ❌ Missing | 🔴 CRITICAL | 2-3 hours |
| **Winner Logic** | ❌ Missing | 🔴 CRITICAL | 1-2 hours |
| **User Auctions** | ❌ Missing | 🔴 HIGH | 2 hours |
| **Search/Filter** | ❌ Missing | 🔴 HIGH | 3 hours |
| **Pagination** | ❌ Missing | 🔴 HIGH | 1 hour |
| **Notifications** | ❌ Missing | 🟡 MEDIUM | 4 hours |
| **Ratings** | ❌ Missing | 🟡 MEDIUM | 3 hours |
| **Watchlist** | ❌ Missing | 🟡 MEDIUM | 2 hours |
| **Comments** | ❌ Missing | 🟢 LOW | 3 hours |
| **Payment** | ❌ Missing | 🟢 LOW | 5-6 hours |
| **Admin** | ❌ Missing | 🟢 LOW | 4 hours |
| **Reporting** | ❌ Missing | 🟢 LOW | 3 hours |

