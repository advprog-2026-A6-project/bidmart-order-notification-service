package id.ac.ui.cs.advprog.ordernotification.service;

import id.ac.ui.cs.advprog.ordernotification.dto.AuthContactPreferencesDto;
import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import id.ac.ui.cs.advprog.ordernotification.model.NotificationPreference;
import id.ac.ui.cs.advprog.ordernotification.model.Order;
import id.ac.ui.cs.advprog.ordernotification.repository.NotificationPreferenceRepository;
import id.ac.ui.cs.advprog.ordernotification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationServiceImplTest {

    @Mock
    private NotificationRepository repository;

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(preferenceRepository.save(any(NotificationPreference.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ReflectionTestUtils.setField(service, "authServiceUrl", "http://localhost:8081/api/internal/users/");
        ReflectionTestUtils.setField(service, "authInternalToken", "test-internal-token");
    }

    @Test
    void testCreateOrderNotification_BothEnabled() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId("user123");
        order.setItemName("Test Item");

        NotificationPreference pref = new NotificationPreference();
        pref.setUserId("user123");
        pref.setEmail("test@example.com");
        pref.setEmailEnabled(true);
        pref.setPushEnabled(true);

        when(preferenceRepository.findByUserId("user123")).thenReturn(Optional.of(pref));

        service.createOrderNotification(order);

        // Verify push notification saved
        verify(repository, atLeastOnce()).save(argThat(n -> "PUSH".equals(n.getPreferenceType())));
        // Verify email notification saved
        verify(repository, atLeastOnce()).save(argThat(n -> "EMAIL".equals(n.getPreferenceType())));
        // Verify email service called
        verify(emailService).sendSimpleEmail(eq("test@example.com"), anyString(), anyString());
        // Verify WebSocket message sent
        verify(messagingTemplate).convertAndSend(eq("/topic/notifications/user123"), anyString());
    }

    @Test
    void testCreateOrderNotification_EmailDisabled_PushEnabled() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId("user123");

        NotificationPreference pref = new NotificationPreference();
        pref.setUserId("user123");
        pref.setEmailEnabled(false);
        pref.setPushEnabled(true);

        when(preferenceRepository.findByUserId("user123")).thenReturn(Optional.of(pref));

        service.createOrderNotification(order);

        verify(repository).save(argThat(n -> "PUSH".equals(n.getPreferenceType())));
        verify(repository, never()).save(argThat(n -> "EMAIL".equals(n.getPreferenceType())));
        verify(emailService, never()).sendSimpleEmail(any(), any(), any());
    }

    @Test
    void testSetPreference() {
        String userId = "user123";
        String email = "new@example.com";

        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(preferenceRepository.save(any(NotificationPreference.class))).thenAnswer(i -> i.getArguments()[0]);

        NotificationPreference result = service.setPreference(userId, email, true, false);

        assertEquals(userId, result.getUserId());
        assertEquals(email, result.getEmail());
        assertTrue(result.isEmailEnabled());
        assertFalse(result.isPushEnabled());
        verify(preferenceRepository).save(any());
    }

    @Test
    void testCreate() {
        Notification notif = new Notification();
        notif.setMessage("Test");
        notif.setType("TYPE");

        when(repository.save(notif)).thenReturn(notif);

        Notification result = service.create(notif);

        assertEquals("Test", result.getMessage());
        verify(repository).save(notif);
    }

    @Test
    void testFindAll() {
        when(repository.findAll()).thenReturn(List.of(new Notification()));

        List<Notification> result = service.findAll();

        assertEquals(1, result.size());
        verify(repository).findAll();
    }

    @Test
    void testCreateOrderNotificationWithNullOrderId() {
        Order order = new Order();
        order.setUserId("user123");
        // ID is null
        service.createOrderNotification(order);
        verify(repository, never()).save(any());
    }

    @Test
    void testCreateOrderNotification_PushDisabled() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId("user123");

        NotificationPreference pref = new NotificationPreference();
        pref.setUserId("user123");
        pref.setPushEnabled(false);

        when(preferenceRepository.findByUserId("user123")).thenReturn(Optional.of(pref));

        service.createOrderNotification(order);

        verify(repository, never()).save(argThat(n -> "PUSH".equals(n.getPreferenceType())));
    }

    @Test
    void testCreateOrderNotification_EmailNull() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId("user123");

        NotificationPreference pref = new NotificationPreference();
        pref.setUserId("user123");
        pref.setEmailEnabled(true);
        pref.setEmail(null);

        when(preferenceRepository.findByUserId("user123")).thenReturn(Optional.of(pref));

        service.createOrderNotification(order);

        verify(repository).save(argThat(n -> "EMAIL_ERROR".equals(n.getPreferenceType())));
    }

    @Test
    void testCreateOrderNotification_EmailNotEnabled() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId("user123");

        NotificationPreference pref = new NotificationPreference();
        pref.setUserId("user123");
        pref.setEmail("test@mail.com");
        pref.setEmailEnabled(false);

        when(preferenceRepository.findByUserId("user123")).thenReturn(Optional.of(pref));

        service.createOrderNotification(order);

        verify(repository, never()).save(argThat(n -> "EMAIL".equals(n.getPreferenceType())));
        verify(repository, never()).save(argThat(n -> "EMAIL_ERROR".equals(n.getPreferenceType())));
        verify(emailService, never()).sendSimpleEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testCreateOrderNotificationWithNullOrder() {
        service.createOrderNotification(null);
        verify(repository, never()).save(any());
    }

    @Test
    void testFindByUserId() {
        Notification notif = new Notification();
        notif.setUserId("user1");
        when(repository.findAll()).thenReturn(List.of(notif));

        List<Notification> result = service.findByUserId("user1");

        assertEquals(1, result.size());
        assertEquals("user1", result.get(0).getUserId());
    }

    @Test
    void testCreateOrderNotification_EmailEnabled_AddressMissing() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId("user123");
        order.setItemName("Test Item");

        NotificationPreference pref = new NotificationPreference();
        pref.setUserId("user123");
        pref.setEmailEnabled(true);
        pref.setEmail("");

        when(preferenceRepository.findByUserId("user123")).thenReturn(Optional.of(pref));

        service.createOrderNotification(order);

        verify(repository).save(argThat(n -> "EMAIL_ERROR".equals(n.getPreferenceType())));
        verify(emailService, never()).sendSimpleEmail(any(), any(), any());
    }

    @Test
    void testGetPreference_Default() {
        when(preferenceRepository.findByUserId("unknown")).thenReturn(Optional.empty());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(AuthContactPreferencesDto.class)))
                .thenReturn(ResponseEntity.ok(null));

        NotificationPreference result = service.getPreference("unknown");

        assertEquals("unknown", result.getUserId());
    }

    @Test
    void testGetPreference_FetchesFromAuthWhenMissingLocally() {
        when(preferenceRepository.findByUserId("42")).thenReturn(Optional.empty());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(AuthContactPreferencesDto.class)))
                .thenReturn(ResponseEntity.ok(new AuthContactPreferencesDto(
                        42L,
                        "seller@example.com",
                        "EMAIL",
                        true,
                        false
                )));

        NotificationPreference result = service.getPreference("42");

        assertEquals("42", result.getUserId());
        assertEquals("seller@example.com", result.getEmail());
        assertTrue(result.isEmailEnabled());
        assertFalse(result.isPushEnabled());
        verify(preferenceRepository).save(any(NotificationPreference.class));
    }

    @Test
    void testCreate_WithStatus() {
        Notification notif = new Notification();
        notif.setStatus("SENT");
        when(repository.save(notif)).thenReturn(notif);

        Notification result = service.create(notif);

        assertEquals("SENT", result.getStatus());
        verify(repository).save(notif);
    }

    @Test
    void testSendNotification() {
        String userId = "user1";
        String message = "Hello World";
        String type = "TEST_TYPE";

        NotificationPreference pref = new NotificationPreference();
        pref.setUserId(userId);
        pref.setPushEnabled(true);

        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(pref));

        service.sendNotification(userId, message, type);

        verify(repository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/notifications/" + userId), eq(message));
    }

    @Test
    void testSendNotification_PushDisabled() {
        String userId = "user1";
        
        NotificationPreference pref = new NotificationPreference();
        pref.setUserId(userId);
        pref.setPushEnabled(false);

        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(pref));

        service.sendNotification(userId, "Message", "TYPE");

        verify(repository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), anyString());
    }
}
