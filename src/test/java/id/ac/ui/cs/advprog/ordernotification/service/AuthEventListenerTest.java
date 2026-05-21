package id.ac.ui.cs.advprog.ordernotification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import id.ac.ui.cs.advprog.ordernotification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuthEventListenerTest {

    private NotificationRepository notificationRepository;
    private AuthEventListener listener;

    @BeforeEach
    void setUp() {
        notificationRepository = mock(NotificationRepository.class);
        listener = new AuthEventListener(new ObjectMapper(), notificationRepository);
    }

    @Test
    void handleAccountDisabledEventCreatesUserNotification() {
        listener.handle(
                "{\"userId\":\"42\",\"email\":\"user@example.com\",\"reason\":\"fraud\"}",
                "auth.event.account_disabled");

        verify(notificationRepository).save(argThat(notification ->
                "42".equals(notification.getUserId())
                        && "user@example.com".equals(notification.getRecipientEmail())
                        && "auth.event.account_disabled".equals(notification.getType())
                        && "SYSTEM".equals(notification.getPreferenceType())
                        && "SENT".equals(notification.getStatus())
                        && notification.getMessage().contains("fraud")));
    }

    @Test
    void handleUserRoleChangedEventCreatesUserNotification() {
        listener.handle(
                "{\"userId\":\"99\",\"email\":\"admin@example.com\",\"action\":\"ADDED\",\"roleName\":\"SELLER\"}",
                "auth.event.user_role_changed");

        verify(notificationRepository).save(argThat(notification ->
                "99".equals(notification.getUserId())
                        && notification.getMessage().contains("ADDED")
                        && notification.getMessage().contains("SELLER")));
    }

    @Test
    void handleRoleCreatedEventCreatesSystemNotification() {
        listener.handle("{\"roleName\":\"MODERATOR\"}", "auth.event.role_created");

        verify(notificationRepository).save(argThat(notification ->
                "system".equals(notification.getUserId())
                        && notification.getMessage().contains("MODERATOR")));
    }

    @Test
    void handlePermissionCreatedEventCreatesSystemNotification() {
        listener.handle("{\"permissionName\":\"order:read\"}", "auth.event.permission_created");

        verify(notificationRepository).save(argThat(notification ->
                "system".equals(notification.getUserId())
                        && notification.getMessage().contains("order:read")));
    }

    @Test
    void handleRolePermissionChangedEventCreatesSystemNotification() {
        listener.handle(
                "{\"action\":\"REMOVED\",\"permissionName\":\"bid:place\",\"roleName\":\"BUYER\"}",
                "auth.event.role_permission_changed");

        verify(notificationRepository).save(argThat(notification ->
                "system".equals(notification.getUserId())
                        && notification.getMessage().contains("REMOVED")
                        && notification.getMessage().contains("bid:place")
                        && notification.getMessage().contains("BUYER")));
    }

    @Test
    void handleUnknownEventCreatesFallbackSystemNotification() {
        listener.handle("{}", "auth.event.unknown");

        verify(notificationRepository).save(argThat(notification ->
                "system".equals(notification.getUserId())
                        && "Auth event diterima".equals(notification.getMessage())));
    }

    @Test
    void handleAccountDisabledEventUsesFallbacksWhenValuesMissing() {
        listener.handle("{}", "auth.event.account_disabled");

        verify(notificationRepository).save(argThat(notification ->
                "system".equals(notification.getUserId())
                        && notification.getRecipientEmail() == null
                        && notification.getMessage().contains("tanpa alasan")));
    }

    @Test
    void handleInvalidPayloadThrowsClearException() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> listener.handle("{invalid-json", "auth.event.account_disabled"));

        assertEquals("Gagal memproses event auth: auth.event.account_disabled", exception.getMessage());
    }
}
