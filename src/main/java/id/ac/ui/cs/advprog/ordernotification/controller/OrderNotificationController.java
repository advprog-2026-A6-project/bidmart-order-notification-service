package id.ac.ui.cs.advprog.ordernotification.controller;

import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import id.ac.ui.cs.advprog.ordernotification.model.NotificationPreference;
import id.ac.ui.cs.advprog.ordernotification.model.AuctionEventMessage;
import id.ac.ui.cs.advprog.ordernotification.model.AuctionFinishedMessage;
import id.ac.ui.cs.advprog.ordernotification.model.Order;
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

    private static final String AUCTION_ID_FIELD = "auctionId";
    private static final String FINAL_PRICE_FIELD = "finalPrice";
    private static final String ITEM_NAME_FIELD = "itemName";
    private static final String SELLER_ID_FIELD = "sellerId";
    private static final String STATUS_FIELD = "status";
    private static final String WINNER_ID_FIELD = "winnerId";

    private final OrderService orderService;
    private final NotificationService notificationService;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public OrderNotificationController(
            OrderService orderService,
            NotificationService notificationService) {
        this.orderService = orderService;
        this.notificationService = notificationService;
    }

    @PostMapping("/auction-finish")
    public ResponseEntity<Object> handleAuctionFinish(@RequestBody Map<String, Object> payload) {
        Long auctionId;
        String userId;
        String itemName;
        Double finalPrice;
        try {
            auctionId = requiredLong(payload, AUCTION_ID_FIELD);
            userId = requiredString(payload, WINNER_ID_FIELD);
            itemName = requiredString(payload, ITEM_NAME_FIELD);
            finalPrice = requiredDouble(payload, FINAL_PRICE_FIELD);
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
    public ResponseEntity<Object> simulateBidPlaced(@RequestBody Map<String, Object> payload) {
        notificationService.createAuctionBidNotification(new AuctionEventMessage(
                null,
                "BidPlaced",
                LocalDateTime.now(),
                payload
        ));
        return ResponseEntity.ok(Map.of(STATUS_FIELD, "BID_PLACED_NOTIFICATION_SENT"));
    }

    @PostMapping("/simulate/outbid")
    public ResponseEntity<Object> simulateOutbid(@RequestBody Map<String, Object> payload) {
        notificationService.createAuctionOutbidNotification(new AuctionEventMessage(
                null,
                "Outbid",
                LocalDateTime.now(),
                payload
        ));
        return ResponseEntity.ok(Map.of(STATUS_FIELD, "OUTBID_NOTIFICATION_SENT"));
    }

    @PostMapping("/simulate/auction-won")
    public ResponseEntity<Object> simulateAuctionWon(@RequestBody Map<String, Object> payload) {
        AuctionFinishedMessage message;
        try {
            message = new AuctionFinishedMessage(
                    requiredLong(payload, AUCTION_ID_FIELD),
                    requiredString(payload, WINNER_ID_FIELD),
                    optionalString(payload, SELLER_ID_FIELD),
                    requiredString(payload, ITEM_NAME_FIELD),
                    requiredDouble(payload, FINAL_PRICE_FIELD),
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
                STATUS_FIELD, "AUCTION_WON_NOTIFICATION_SENT_AND_ORDER_CREATED",
                "orderId", order.getId(),
                "orderStatus", order.getStatus()
        ));
    }

    private ResponseEntity<Object> badRequest(String message) {
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
