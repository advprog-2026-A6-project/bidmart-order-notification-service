package id.ac.ui.cs.advprog.ordernotification.service.delivery;

import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import id.ac.ui.cs.advprog.ordernotification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PushNotificationStrategyTest {

    @Mock
    private NotificationRepository repository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private PushNotificationStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        strategy = new PushNotificationStrategy(repository, messagingTemplate);
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testSupports_Push() {
        assertTrue(strategy.supports("PUSH"));
        assertTrue(strategy.supports("push"));
    }

    @Test
    void testSupports_NonPush() {
        assertFalse(strategy.supports("EMAIL"));
        assertFalse(strategy.supports("SMS"));
    }

    @Test
    void testDeliver() {
        Notification result = strategy.deliver("user1", "Push message", "ORDER_CREATED", null);

        assertNotNull(result);
        assertEquals("user1", result.getUserId());
        assertEquals("Push message", result.getMessage());
        assertEquals("ORDER_CREATED", result.getType());
        assertEquals("PUSH", result.getPreferenceType());
        assertEquals("SENT", result.getStatus());
        assertNull(result.getRecipientEmail());
        assertNotNull(result.getCreatedAt());

        verify(repository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSend("/topic/notifications/user1", "Push message");
    }
}
