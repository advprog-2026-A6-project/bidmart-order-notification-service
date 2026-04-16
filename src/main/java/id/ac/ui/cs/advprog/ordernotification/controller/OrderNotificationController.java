package id.ac.ui.cs.advprog.ordernotification.controller;

import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import id.ac.ui.cs.advprog.ordernotification.model.NotificationPreference;
import id.ac.ui.cs.advprog.ordernotification.model.Order;
import id.ac.ui.cs.advprog.ordernotification.service.NotificationService;
import id.ac.ui.cs.advprog.ordernotification.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order-notification")
public class OrderNotificationController {

    private final OrderService orderService;
    private final NotificationService notificationService;

    public OrderNotificationController(OrderService orderService, NotificationService notificationService) {
        this.orderService = orderService;
        this.notificationService = notificationService;
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
            @RequestParam(required = false) String email,
            @RequestParam boolean emailEnabled,
            @RequestParam boolean pushEnabled) {
        NotificationPreference pref = notificationService.setPreference(userId, email, emailEnabled, pushEnabled);
        return ResponseEntity.ok(pref);
    }

    @GetMapping("/preferences/{userId}")
    public ResponseEntity<NotificationPreference> getPreference(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getPreference(userId));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/notifications/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.findByUserId(userId));
    }
}
