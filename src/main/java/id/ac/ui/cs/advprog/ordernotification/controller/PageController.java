package id.ac.ui.cs.advprog.ordernotification.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "orderList";
    }

    @GetMapping("/orders")
    public String ordersPage() {
        return "orderList";
    }

    @GetMapping("/create-order")
    public String createOrderPage() {
        return "createOrder";
    }

    @GetMapping("/notifications")
    public String notificationsPage() {
        return "notificationList";
    }
}