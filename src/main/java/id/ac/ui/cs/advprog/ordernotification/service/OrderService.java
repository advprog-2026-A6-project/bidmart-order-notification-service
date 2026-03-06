package id.ac.ui.cs.advprog.ordernotification.service;

import id.ac.ui.cs.advprog.ordernotification.model.Order;

import java.util.List;

public interface OrderService {
    Order create(Order order);
    List<Order> findAll();
}