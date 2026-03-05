package id.ac.ui.cs.advprog.ordernotification.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    @Test
    void testGetterSetter() {
        Notification notif = new Notification();
        notif.setId(1L);
        notif.setMessage("Test");
        notif.setType("ORDER_CREATED");

        assertEquals(1L, notif.getId());
        assertEquals("Test", notif.getMessage());
        assertEquals("ORDER_CREATED", notif.getType());
    }
}