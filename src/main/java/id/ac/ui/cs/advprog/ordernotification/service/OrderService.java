package id.ac.ui.cs.advprog.ordernotification.service;

import id.ac.ui.cs.advprog.ordernotification.model.Order;

import java.util.List;

public interface OrderService {
    Order create(Order order);
    Order createAutomaticOrder(Long auctionId, String userId, String itemName, Double totalPrice);
    List<Order> findAll();
}