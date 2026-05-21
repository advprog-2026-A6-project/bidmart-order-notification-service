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
    public ResponseEntity<?> handleAuctionFinish(@RequestBody Map<String, Object> payload) {
        Long auctionId;
        String userId;
        String itemName;
        Double finalPrice;
        try {
            auctionId = requiredLong(payload, "auctionId");
            userId = requiredString(payload, "winnerId");
            itemName = requiredString(payload, "itemName");
            finalPrice = requiredDouble(payload, "finalPrice");
        } catch (IllegalArgumentException exception) {
            return badRequest(exception.getMessage());
        }

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
    public ResponseEntity<?> simulateBidPlaced(@RequestBody Map<String, Object> payload) {
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
    public ResponseEntity<?> simulateOutbid(@RequestBody Map<String, Object> payload) {
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
    public ResponseEntity<?> simulateAuctionWon(@RequestBody Map<String, Object> payload) {
        if (!featureFlags.isSimulationEndpointsEnabled()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        AuctionFinishedMessage message;
        try {
            message = new AuctionFinishedMessage(
                    requiredLong(payload, "auctionId"),
                    requiredString(payload, "winnerId"),
                    optionalString(payload, "sellerId"),
                    requiredString(payload, "itemName"),
                    requiredDouble(payload, "finalPrice"),
                    null
            );
        } catch (IllegalArgumentException exception) {
            return badRequest(exception.getMessage());
        }
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

    private ResponseEntity<Map<String, String>> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    private String requiredString(Map<String, Object> payload, String field) {
        Object value = payload.get(field);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException("Field '" + field + "' is required");
        }
        return value.toString();
    }

    private String optionalString(Map<String, Object> payload, String field) {
        Object value = payload.get(field);
        return value == null || value.toString().isBlank() ? null : value.toString();
    }

    private Long requiredLong(Map<String, Object> payload, String field) {
        Object value = payload.get(field);
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.valueOf(requiredString(payload, field));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Field '" + field + "' must be a valid number", exception);
        }
    }

    private Double requiredDouble(Map<String, Object> payload, String field) {
        Object value = payload.get(field);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.valueOf(requiredString(payload, field));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Field '" + field + "' must be a valid number", exception);
        }
    }
}
