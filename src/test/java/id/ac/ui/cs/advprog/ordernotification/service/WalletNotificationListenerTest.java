package id.ac.ui.cs.advprog.ordernotification.service;

import id.ac.ui.cs.advprog.ordernotification.event.WalletNotificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class WalletNotificationListenerTest {

    private NotificationService notificationService;
    private WalletNotificationListener listener;

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        listener = new WalletNotificationListener(notificationService);
    }

    @Test
    void testHandleWalletNotification() {
        WalletNotificationEvent event = new WalletNotificationEvent(
                "1",
                "TOPUP",
                BigDecimal.valueOf(100000),
                "Top-up dari Bank BCA",
                LocalDateTime.parse("2026-05-20T18:35:00.123456"));

        listener.handleWalletNotification(event);

        verify(notificationService, times(1)).createWalletNotification(event);
    }
}
