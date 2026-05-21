package id.ac.ui.cs.advprog.ordernotification.service;

import id.ac.ui.cs.advprog.ordernotification.model.AuctionEventMessage;
import id.ac.ui.cs.advprog.ordernotification.model.AuctionFinishedMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AuctionActivityListenerTest {

    private NotificationService notificationService;
    private AuctionActivityListener listener;

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        listener = new AuctionActivityListener(notificationService);
    }

    @Test
    void handleAuctionActivityDelegatesBidPlaced() {
        AuctionEventMessage event = event("BidPlaced");

        listener.handleAuctionActivity(event);

        verify(notificationService).createAuctionBidNotification(event);
    }

    @Test
    void handleAuctionActivityDelegatesOutbid() {
        AuctionEventMessage event = event("Outbid");

        listener.handleAuctionActivity(event);

        verify(notificationService).createAuctionOutbidNotification(event);
    }

    @Test
    void handleAuctionActivityIgnoresUnknownOrNullEvent() {
        AuctionEventMessage unknown = event("AuctionClosed");

        listener.handleAuctionActivity(null);
        listener.handleAuctionActivity(new AuctionEventMessage());
        listener.handleAuctionActivity(unknown);

        verify(notificationService, never()).createAuctionBidNotification(unknown);
        verify(notificationService, never()).createAuctionOutbidNotification(unknown);
    }

    @Test
    void handleAuctionWonDelegatesToNotificationService() {
        AuctionFinishedMessage message = new AuctionFinishedMessage(
                1L,
                "winner1",
                "seller1",
                "Laptop",
                100000.0,
                2L);

        listener.handleAuctionWon(message);

        verify(notificationService).createAuctionWonNotification(message);
    }

    private AuctionEventMessage event(String type) {
        return new AuctionEventMessage(1L, type, LocalDateTime.now(), Map.of("auctionId", 10L));
    }
}
