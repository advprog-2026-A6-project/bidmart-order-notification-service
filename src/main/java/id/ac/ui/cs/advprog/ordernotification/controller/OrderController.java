package id.ac.ui.cs.advprog.ordernotification.controller;

import id.ac.ui.cs.advprog.ordernotification.model.Order;
import id.ac.ui.cs.advprog.ordernotification.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<Order> getOrders() {
        return service.findAll();
    }

    @PostMapping
    public Order createOrder(
            @RequestParam String userId,
            @RequestParam double totalPrice) {

        Order order = new Order();
        order.setUserId(userId);
        order.setTotalPrice(totalPrice);

        return service.create(order);
    }
}