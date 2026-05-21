package id.ac.ui.cs.advprog.ordernotification.service.delivery;

import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import id.ac.ui.cs.advprog.ordernotification.repository.NotificationRepository;
import id.ac.ui.cs.advprog.ordernotification.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmailNotificationStrategyTest {

    @Mock
    private NotificationRepository repository;

    @Mock
    private EmailService emailService;

    private EmailNotificationStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        strategy = new EmailNotificationStrategy(repository, emailService);
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testSupports_Email() {
        assertTrue(strategy.supports("EMAIL"));
        assertTrue(strategy.supports("email"));
    }

    @Test
    void testSupports_NonEmail() {
        assertFalse(strategy.supports("PUSH"));
        assertFalse(strategy.supports("SMS"));
        assertFalse(strategy.supports(null));
    }

    @Test
    void testDeliver_OrderCreatedType() {
        Notification result = strategy.deliver("user1", "Order created message", "ORDER_CREATED", "user1@example.com");

        assertNotNull(result);
        assertEquals("user1", result.getUserId());
        assertEquals("EMAIL", result.getPreferenceType());
        assertEquals("SENT", result.getStatus());
        assertEquals("user1@example.com", result.getRecipientEmail());

        // Verify email was sent with the correct subject for ORDER_CREATED
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendSimpleEmail(eq("user1@example.com"), subjectCaptor.capture(), eq("Order created message"));
        assertEquals("[BidMart] Konfirmasi Pesanan Otomatis", subjectCaptor.getValue());
    }

    @Test
    void testDeliver_OrderShippedType() {
        Notification result = strategy.deliver("user2", "Shipped message", "ORDER_SHIPPED", "user2@example.com");

        assertNotNull(result);
        assertEquals("SENT", result.getStatus());

        // Verify email subject for ORDER_ prefixed types (not ORDER_CREATED)
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendSimpleEmail(eq("user2@example.com"), subjectCaptor.capture(), eq("Shipped message"));
        assertEquals("[BidMart] Pembaruan Pesanan - SHIPPED", subjectCaptor.getValue());
    }

    @Test
    void testDeliver_OrderDisputedType() {
        Notification result = strategy.deliver("user3", "Dispute message", "ORDER_DISPUTED", "user3@example.com");

        assertNotNull(result);

        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendSimpleEmail(eq("user3@example.com"), subjectCaptor.capture(), eq("Dispute message"));
        assertEquals("[BidMart] Pembaruan Pesanan - DISPUTED", subjectCaptor.getValue());
    }

    @Test
    void testDeliver_GenericType() {
        Notification result = strategy.deliver("user4", "Generic message", "PROMO", "user4@example.com");

        assertNotNull(result);

        // Verify generic subject (not ORDER_CREATED, not starting with ORDER_)
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendSimpleEmail(eq("user4@example.com"), subjectCaptor.capture(), eq("Generic message"));
        assertEquals("[BidMart] Notifikasi - PROMO", subjectCaptor.getValue());
    }

    @Test
    void testDeliver_NullType() {
        Notification result = strategy.deliver("user5", "Null type message", null, "user5@example.com");

        assertNotNull(result);

        // When type is null, it should use the generic subject format
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendSimpleEmail(eq("user5@example.com"), subjectCaptor.capture(), eq("Null type message"));
        assertEquals("[BidMart] Notifikasi - null", subjectCaptor.getValue());
    }

    @Test
    void testDeliver_SavesNotificationTwice() {
        strategy.deliver("user6", "Test message", "TEST", "user6@example.com");

        // Save called twice: once for SENDING, once for SENT
        verify(repository, times(2)).save(any(Notification.class));
    }
}
