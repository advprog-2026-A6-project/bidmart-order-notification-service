package id.ac.ui.cs.advprog.ordernotification.model;

import java.time.LocalDateTime;

public class NotificationFactory {

    private NotificationFactory() {
    }

    public static Notification createPushNotification(String userId, String message, String type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setType(type);
        notification.setPreferenceType("PUSH");
        notification.setStatus("SENT");
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }

    public static Notification createEmailNotification(String userId, String message, String type, String recipientEmail, String status) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setType(type);
        notification.setPreferenceType("EMAIL");
        notification.setStatus(status);
        notification.setRecipientEmail(recipientEmail);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
    
    public static Notification createEmailErrorNotification(String userId, String message, String type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setType(type);
        notification.setPreferenceType("EMAIL_ERROR");
        notification.setStatus("FAILED");
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
}
