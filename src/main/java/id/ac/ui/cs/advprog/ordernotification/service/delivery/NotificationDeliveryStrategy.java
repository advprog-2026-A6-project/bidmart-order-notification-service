package id.ac.ui.cs.advprog.ordernotification.service.delivery;

import id.ac.ui.cs.advprog.ordernotification.model.Notification;

public interface NotificationDeliveryStrategy {
    boolean supports(String preferenceType);
    Notification deliver(String userId, String message, String type, String recipient);
}
