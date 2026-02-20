package id.ac.ui.cs.advprog.ordernotification.feature;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NotificationTest {

    @Test
    void testCreateNotification() {
        Notification notification = new Notification("Test Message", "INFO");

        assertNotNull(notification);
        assertEquals("Test Message", notification.getMessage());
        assertEquals("INFO", notification.getType());
    }

    @Test
    void testNotificationNotEmpty() {
        Notification notification = new Notification("Hello", "WIN");

        assertFalse(notification.getMessage().isEmpty());
        assertFalse(notification.getType().isEmpty());
    }

    @Test
    void testNotificationDifferentValues() {
        Notification notif1 = new Notification("A", "INFO");
        Notification notif2 = new Notification("B", "ALERT");

        assertNotEquals(notif1.getMessage(), notif2.getMessage());
    }
}