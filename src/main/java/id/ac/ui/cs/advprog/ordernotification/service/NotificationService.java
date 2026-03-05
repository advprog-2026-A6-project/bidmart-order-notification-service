package id.ac.ui.cs.advprog.ordernotification.service;

import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import id.ac.ui.cs.advprog.ordernotification.model.Order;

import java.util.List;

public interface NotificationService {
    Notification create(Notification notification);
    List<Notification> findAll();
    void createOrderNotification(Order order);
}