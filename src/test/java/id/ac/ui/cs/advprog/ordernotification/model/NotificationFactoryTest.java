package id.ac.ui.cs.advprog.ordernotification.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NotificationFactoryTest {

    @Test
    void testCreatePushNotification() {
        Notification notif = NotificationFactory.createPushNotification("user1", "Push message", "ORDER_CREATED");
        assertNotNull(notif);
        assertEquals("user1", notif.getUserId());
        assertEquals("Push message", notif.getMessage());
        assertEquals("ORDER_CREATED", notif.getType());
        assertEquals("PUSH", notif.getPreferenceType());
        assertEquals("SENT", notif.getStatus());
        assertNotNull(notif.getCreatedAt());
    }

    @Test
    void testCreateEmailNotification() {
        Notification notif = NotificationFactory.createEmailNotification("user2", "Email message", "ORDER_SHIPPED", "user2@example.com", "SENDING");
        assertNotNull(notif);
        assertEquals("user2", notif.getUserId());
        assertEquals("Email message", notif.getMessage());
        assertEquals("ORDER_SHIPPED", notif.getType());
        assertEquals("EMAIL", notif.getPreferenceType());
        assertEquals("SENDING", notif.getStatus());
        assertEquals("user2@example.com", notif.getRecipientEmail());
        assertNotNull(notif.getCreatedAt());
    }

    @Test
    void testCreateEmailErrorNotification() {
        Notification notif = NotificationFactory.createEmailErrorNotification("user3", "Error message", "ORDER_COMPLETED");
        assertNotNull(notif);
        assertEquals("user3", notif.getUserId());
        assertEquals("Error message", notif.getMessage());
        assertEquals("ORDER_COMPLETED", notif.getType());
        assertEquals("EMAIL_ERROR", notif.getPreferenceType());
        assertEquals("FAILED", notif.getStatus());
        assertNotNull(notif.getCreatedAt());
    }
}
