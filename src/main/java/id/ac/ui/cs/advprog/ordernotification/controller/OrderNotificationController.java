package id.ac.ui.cs.advprog.ordernotification.controller;

import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import id.ac.ui.cs.advprog.ordernotification.model.NotificationPreference;
import id.ac.ui.cs.advprog.ordernotification.model.AuctionEventMessage;
import id.ac.ui.cs.advprog.ordernotification.model.AuctionFinishedMessage;
import id.ac.ui.cs.advprog.ordernotification.model.Order;
import id.ac.ui.cs.advprog.ordernotification.config.FeatureFlagProperties;
import id.ac.ui.cs.advprog.ordernotification.service.NotificationService;
import id.ac.ui.cs.advprog.ordernotification.service.OrderService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/order-notification")
public class OrderNotificationController {

    private final OrderService orderService;
    private final NotificationService notificationService;
    private final FeatureFlagProperties featureFlags;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public OrderNotificationController(
            OrderService orderService,
            NotificationService notificationService,
            FeatureFlagProperties featureFlags) {
        this.orderService = orderService;
        this.notificationService = notificationService;
        this.featureFlags = featureFlags;
    }

    @PostMapping("/auction-finish")
    public ResponseEntity<Order> handleAuctionFinish(@RequestBody Map<String, Object> payload) {
        Long auctionId = Long.valueOf(payload.get("auctionId").toString());
        String userId = payload.get("winnerId").toString();
        String itemName = payload.get("itemName").toString();
        Double finalPrice = Double.valueOf(payload.get("finalPrice").toString());

        Order order = orderService.createAutomaticOrder(auctionId, userId, itemName, finalPrice);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/preferences/{userId}")
    public ResponseEntity<NotificationPreference> updatePreference(
            @PathVariable String userId,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestParam(required = false) String email,
            @RequestParam boolean emailEnabled,
            @RequestParam boolean pushEnabled) {
        if (xUserId != null && !xUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        NotificationPreference pref = notificationService.setPreference(userId, email, emailEnabled, pushEnabled);
        return ResponseEntity.ok(pref);
    }

    @GetMapping("/preferences/{userId}")
    public ResponseEntity<NotificationPreference> getPreference(
            @PathVariable String userId,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId) {
        if (xUserId != null && !xUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(notificationService.getPreference(userId));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<Order> getOrderById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId) {
        Order order = orderService.findById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        if (xUserId != null && !xUserId.equals(order.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(order);
    }

    @GetMapping("/orders/user/{userId}")
    public ResponseEntity<List<Order>> getUserOrders(
            @PathVariable String userId,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId) {
        if (xUserId != null && !xUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(orderService.findByUserId(userId));
    }

    @GetMapping("/notifications/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(
            @PathVariable String userId,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId) {
        if (xUserId != null && !xUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(notificationService.findByUserId(userId));
    }

    @PostMapping("/orders/{id}/tracking")
    public ResponseEntity<Order> updateTracking(
            @PathVariable Long id,
            @RequestParam String trackingNumber) {
        return ResponseEntity.ok(orderService.updateTrackingNumber(id, trackingNumber));
    }

    @PostMapping("/orders/{id}/packed")
    public ResponseEntity<Order> markPacked(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.markPacked(id));
    }

    @PostMapping("/orders/{id}/confirm")
    public ResponseEntity<Order> confirmReceipt(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.confirmReceipt(id));
    }

    @PostMapping("/orders/{id}/dispute")
    public ResponseEntity<Order> submitDispute(
            @PathVariable Long id,
            @RequestParam String reason) {
        return ResponseEntity.ok(orderService.submitDispute(id, reason));
    }

    @PostMapping("/simulate/bid-placed")
    public ResponseEntity<Map<String, String>> simulateBidPlaced(@RequestBody Map<String, Object> payload) {
        if (!featureFlags.isSimulationEndpointsEnabled()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        notificationService.createAuctionBidNotification(new AuctionEventMessage(
                null,
                "BidPlaced",
                LocalDateTime.now(),
                payload
        ));
        return ResponseEntity.ok(Map.of("status", "BID_PLACED_NOTIFICATION_SENT"));
    }

    @PostMapping("/simulate/outbid")
    public ResponseEntity<Map<String, String>> simulateOutbid(@RequestBody Map<String, Object> payload) {
        if (!featureFlags.isSimulationEndpointsEnabled()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        notificationService.createAuctionOutbidNotification(new AuctionEventMessage(
                null,
                "Outbid",
                LocalDateTime.now(),
                payload
        ));
        return ResponseEntity.ok(Map.of("status", "OUTBID_NOTIFICATION_SENT"));
    }

    @PostMapping("/simulate/auction-won")
    public ResponseEntity<Map<String, Object>> simulateAuctionWon(@RequestBody Map<String, Object> payload) {
        if (!featureFlags.isSimulationEndpointsEnabled()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        AuctionFinishedMessage message = new AuctionFinishedMessage(
                Long.valueOf(payload.get("auctionId").toString()),
                payload.get("winnerId").toString(),
                payload.get("sellerId") == null ? null : payload.get("sellerId").toString(),
                payload.get("itemName").toString(),
                Double.valueOf(payload.get("finalPrice").toString()),
                null
        );
        notificationService.createAuctionWonNotification(message);
        Order order = orderService.createAutomaticOrder(
                message.getAuctionId(),
                message.getWinnerId(),
                message.getItemName(),
                message.getFinalPrice()
        );
        return ResponseEntity.ok(Map.of(
                "status", "AUCTION_WON_NOTIFICATION_SENT_AND_ORDER_CREATED",
                "orderId", order.getId(),
                "orderStatus", order.getStatus()
        ));
    }
}
