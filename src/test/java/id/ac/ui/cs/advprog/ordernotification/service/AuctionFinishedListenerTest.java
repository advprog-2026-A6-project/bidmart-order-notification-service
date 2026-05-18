package id.ac.ui.cs.advprog.ordernotification.service;

import id.ac.ui.cs.advprog.ordernotification.model.AuctionFinishedMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class AuctionFinishedListenerTest {

    private OrderService orderService;
    private AuctionFinishedListener listener;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        listener = new AuctionFinishedListener(orderService);
    }

    @Test
    void testHandleAuctionFinished() {
        AuctionFinishedMessage message = new AuctionFinishedMessage();
        message.setAuctionId(123L);
        message.setWinnerId("user-uuid");
        message.setItemName("Test Item");
        message.setFinalPrice(500.0);

        listener.handleAuctionFinished(message);

        verify(orderService, times(1)).createAutomaticOrder(123L, "user-uuid", "Test Item", 500.0);
    }
}
