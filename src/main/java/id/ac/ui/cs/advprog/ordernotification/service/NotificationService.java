package id.ac.ui.cs.advprog.ordernotification.service;

import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import id.ac.ui.cs.advprog.ordernotification.model.NotificationPreference;
import id.ac.ui.cs.advprog.ordernotification.model.Order;

import java.util.List;

public interface NotificationService {
    Notification create(Notification notification);
    List<Notification> findAll();
    List<Notification> findByUserId(String userId);
    void createOrderNotification(Order order);
    NotificationPreference setPreference(String userId, String email, boolean emailEnabled, boolean pushEnabled);
    NotificationPreference getPreference(String userId);
    void sendNotification(String userId, String message, String type);
}