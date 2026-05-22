package id.ac.ui.cs.advprog.ordernotification.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AuctionFinishedMessageTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        AuctionFinishedMessage message = new AuctionFinishedMessage();
        message.setAuctionId(1L);
        message.setWinnerId("winner1");
        message.setSellerId("seller1");
        message.setItemName("Laptop");
        message.setFinalPrice(5000.0);
        message.setWinningBidId(9L);

        assertEquals(1L, message.getAuctionId());
        assertEquals("winner1", message.getWinnerId());
        assertEquals("seller1", message.getSellerId());
        assertEquals("Laptop", message.getItemName());
        assertEquals(5000.0, message.getFinalPrice());
        assertEquals(9L, message.getWinningBidId());
    }

    @Test
    void testAllArgsConstructor() {
        AuctionFinishedMessage message = new AuctionFinishedMessage(
                2L,
                "winner2",
                "seller2",
                "Camera",
                7500.0,
                10L
        );

        assertEquals(2L, message.getAuctionId());
        assertEquals("winner2", message.getWinnerId());
        assertEquals("seller2", message.getSellerId());
        assertEquals("Camera", message.getItemName());
        assertEquals(7500.0, message.getFinalPrice());
        assertEquals(10L, message.getWinningBidId());
    }

    @Test
    void testBackwardCompatibleConstructor() {
        AuctionFinishedMessage message = new AuctionFinishedMessage(3L, "winner3", "Keyboard", 8500.0);

        assertEquals(3L, message.getAuctionId());
        assertEquals("winner3", message.getWinnerId());
        assertNull(message.getSellerId());
        assertEquals("Keyboard", message.getItemName());
        assertEquals(8500.0, message.getFinalPrice());
        assertNull(message.getWinningBidId());
    }
}
