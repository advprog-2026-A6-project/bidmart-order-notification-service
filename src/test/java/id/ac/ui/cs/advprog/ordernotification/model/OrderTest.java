package id.ac.ui.cs.advprog.ordernotification.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void testGetterSetter() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId("user1");
        order.setTotalPrice(100.0);
        order.setStatus("CREATED");

        assertEquals(1L, order.getId());
        assertEquals("user1", order.getUserId());
        assertEquals(100.0, order.getTotalPrice());
        assertEquals("CREATED", order.getStatus());
    }
}