package id.ac.ui.cs.advprog.ordernotification.controller;

import id.ac.ui.cs.advprog.ordernotification.model.Order;
import id.ac.ui.cs.advprog.ordernotification.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping("/create")
    public String createPage(Model model) {
        model.addAttribute("order", new Order());
        return "createOrder";
    }

    @PostMapping("/create")
    public String createPost(@ModelAttribute Order order) {
        service.create(order);
        return "redirect:/order/list";
    }

    @GetMapping("/list")
    public String listPage(Model model) {
        List<Order> orders = service.findAll();
        model.addAttribute("orders", orders);
        return "orderList";
    }
}