package id.ac.ui.cs.advprog.ordernotification.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    @Test
    void testGetterSetter() {
        Notification notif = new Notification();
        notif.setId(1L);
        notif.setUserId("user123");
        notif.setRecipientEmail("test@example.com");
        notif.setMessage("Test Message");
        notif.setType("ORDER_CREATED");
        notif.setStatus("SENT");
        notif.setPreferenceType("PUSH");
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        notif.setCreatedAt(now);

        assertEquals(1L, notif.getId());
        assertEquals("user123", notif.getUserId());
        assertEquals("test@example.com", notif.getRecipientEmail());
        assertEquals("Test Message", notif.getMessage());
        assertEquals("ORDER_CREATED", notif.getType());
        assertEquals("SENT", notif.getStatus());
        assertEquals("PUSH", notif.getPreferenceType());
        assertEquals(now, notif.getCreatedAt());
    }

    @Test
    void testBuilder() {
        java.time.LocalDateTime time = java.time.LocalDateTime.of(2026, 5, 20, 12, 0);
        Notification notif = Notification.builder()
                .userId("user789")
                .recipientEmail("user789@example.com")
                .message("Builder Message")
                .type("ORDER_SHIPPED")
                .status("PENDING")
                .preferenceType("EMAIL")
                .createdAt(time)
                .build();

        assertNull(notif.getId()); // ID should not be set by builder
        assertEquals("user789", notif.getUserId());
        assertEquals("user789@example.com", notif.getRecipientEmail());
        assertEquals("Builder Message", notif.getMessage());
        assertEquals("ORDER_SHIPPED", notif.getType());
        assertEquals("PENDING", notif.getStatus());
        assertEquals("EMAIL", notif.getPreferenceType());
        assertEquals(time, notif.getCreatedAt());
    }
}