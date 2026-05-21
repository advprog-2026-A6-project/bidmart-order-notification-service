package id.ac.ui.cs.advprog.ordernotification.service;

import id.ac.ui.cs.advprog.ordernotification.config.FeatureFlagProperties;
import id.ac.ui.cs.advprog.ordernotification.dto.AuthContactPreferencesDto;
import id.ac.ui.cs.advprog.ordernotification.model.AuctionEventMessage;
import id.ac.ui.cs.advprog.ordernotification.model.AuctionFinishedMessage;
import id.ac.ui.cs.advprog.ordernotification.model.AuctionParticipant;
import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import id.ac.ui.cs.advprog.ordernotification.model.NotificationPreference;
import id.ac.ui.cs.advprog.ordernotification.model.Order;
import id.ac.ui.cs.advprog.ordernotification.event.WalletNotificationEvent;
import id.ac.ui.cs.advprog.ordernotification.repository.AuctionParticipantRepository;
import id.ac.ui.cs.advprog.ordernotification.repository.NotificationPreferenceRepository;
import id.ac.ui.cs.advprog.ordernotification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

    private NotificationServiceImpl service;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AuctionParticipantRepository auctionParticipantRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new NotificationServiceImpl(
                repository,
                preferenceRepository,
                emailService,
                messagingTemplate,
                Optional.empty(),
                Optional.of(auctionParticipantRepository),
                restTemplate,
                "",
                "bidmart-internal-dev-token",
                new FeatureFlagProperties());
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
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
    void testCreateOrderNotification_FeatureFlagsDisabled() {
        FeatureFlagProperties flags = new FeatureFlagProperties();
        flags.setPushNotificationEnabled(false);
        flags.setEmailNotificationEnabled(false);
        NotificationServiceImpl disabledFeatureService = new NotificationServiceImpl(
                repository,
                preferenceRepository,
                emailService,
                messagingTemplate,
                Optional.empty(),
                restTemplate,
                "",
                "token",
                flags);

        Order order = new Order();
        order.setId(1L);
        order.setUserId("user123");
        order.setItemName("Test Item");

        NotificationPreference pref = new NotificationPreference();
        pref.setUserId("user123");
        pref.setEmail("test@mail.com");
        pref.setEmailEnabled(true);
        pref.setPushEnabled(true);

        when(preferenceRepository.findByUserId("user123")).thenReturn(Optional.of(pref));

        disabledFeatureService.createOrderNotification(order);

        verify(repository).save(argThat(n -> "EMAIL_ERROR".equals(n.getPreferenceType())));
        verify(emailService, never()).sendSimpleEmail(any(), any(), any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), anyString());
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
        when(repository.findByUserId("user1")).thenReturn(List.of(notif));

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

        NotificationPreference result = service.getPreference("unknown");

        assertEquals("unknown", result.getUserId());
    }

    @Test
    void testGetPreference_FetchesFromAuthWhenMissingLocally() {
        NotificationServiceImpl authAwareService = new NotificationServiceImpl(
                repository,
                preferenceRepository,
                emailService,
                messagingTemplate,
                Optional.empty(),
                restTemplate,
                "http://localhost:8081/api/internal/users/",
                "test-internal-token",
                new FeatureFlagProperties());

        AuthContactPreferencesDto authPreference = new AuthContactPreferencesDto(
                42L,
                "seller@example.com",
                "EMAIL",
                true,
                false);

        when(preferenceRepository.findByUserId("42")).thenReturn(Optional.empty());
        when(preferenceRepository.save(any(NotificationPreference.class))).thenAnswer(i -> i.getArguments()[0]);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(AuthContactPreferencesDto.class)))
                .thenReturn(ResponseEntity.ok(authPreference));

        NotificationPreference result = authAwareService.getPreference("42");

        assertEquals("42", result.getUserId());
        assertEquals("seller@example.com", result.getEmail());
        assertTrue(result.isEmailEnabled());
        assertFalse(result.isPushEnabled());
        verify(preferenceRepository).save(any(NotificationPreference.class));
    }

    @Test
    void testGetPreference_ReturnsDefaultWhenAuthReturnsNoBody() {
        NotificationServiceImpl authAwareService = new NotificationServiceImpl(
                repository,
                preferenceRepository,
                emailService,
                messagingTemplate,
                Optional.empty(),
                restTemplate,
                "http://localhost:8081/api/internal/users/",
                "test-internal-token",
                new FeatureFlagProperties());

        when(preferenceRepository.findByUserId("43")).thenReturn(Optional.empty());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(AuthContactPreferencesDto.class)))
                .thenReturn(ResponseEntity.ok(null));

        NotificationPreference result = authAwareService.getPreference("43");

        assertEquals("43", result.getUserId());
        verify(preferenceRepository, never()).save(any(NotificationPreference.class));
    }

    @Test
    void testGetPreference_ReturnsDefaultWhenAuthRequestFails() {
        NotificationServiceImpl authAwareService = new NotificationServiceImpl(
                repository,
                preferenceRepository,
                emailService,
                messagingTemplate,
                Optional.empty(),
                restTemplate,
                "http://localhost:8081/api/internal/users/",
                "test-internal-token",
                new FeatureFlagProperties());

        when(preferenceRepository.findByUserId("44")).thenReturn(Optional.empty());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(AuthContactPreferencesDto.class)))
                .thenThrow(new RestClientException("auth unavailable"));

        NotificationPreference result = authAwareService.getPreference("44");

        assertEquals("44", result.getUserId());
        verify(preferenceRepository, never()).save(any(NotificationPreference.class));
    }

    @Test
    void testGetPreference_DoesNotSendBlankInternalToken() {
        NotificationServiceImpl authAwareService = new NotificationServiceImpl(
                repository,
                preferenceRepository,
                emailService,
                messagingTemplate,
                Optional.empty(),
                restTemplate,
                "http://localhost:8081/api/internal/users/",
                " ",
                new FeatureFlagProperties());

        AuthContactPreferencesDto authPreference = new AuthContactPreferencesDto(
                45L,
                "blank-token@example.com",
                "EMAIL",
                false,
                true);

        when(preferenceRepository.findByUserId("45")).thenReturn(Optional.empty());
        when(preferenceRepository.save(any(NotificationPreference.class))).thenAnswer(i -> i.getArguments()[0]);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                argThat(entity -> !entity.getHeaders().containsKey("X-Internal-Service-Token")),
                eq(AuthContactPreferencesDto.class)))
                .thenReturn(ResponseEntity.ok(authPreference));

        NotificationPreference result = authAwareService.getPreference("45");

        assertEquals("blank-token@example.com", result.getEmail());
        assertFalse(result.isEmailEnabled());
        assertTrue(result.isPushEnabled());
    }

    @Test
    void testGetPreference_DoesNotSendNullInternalToken() {
        NotificationServiceImpl authAwareService = new NotificationServiceImpl(
                repository,
                preferenceRepository,
                emailService,
                messagingTemplate,
                Optional.empty(),
                restTemplate,
                "http://localhost:8081/api/internal/users/",
                null,
                new FeatureFlagProperties());

        AuthContactPreferencesDto authPreference = new AuthContactPreferencesDto(
                46L,
                "null-token@example.com",
                "EMAIL",
                true,
                true);

        when(preferenceRepository.findByUserId("46")).thenReturn(Optional.empty());
        when(preferenceRepository.save(any(NotificationPreference.class))).thenAnswer(i -> i.getArguments()[0]);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                argThat(entity -> !entity.getHeaders().containsKey("X-Internal-Service-Token")),
                eq(AuthContactPreferencesDto.class)))
                .thenReturn(ResponseEntity.ok(authPreference));

        NotificationPreference result = authAwareService.getPreference("46");

        assertEquals("null-token@example.com", result.getEmail());
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

    @Test
    void testExecuteDelivery_FallbackWhenNoStrategyMatches() {
        // Create a service with strategies that don't match any channel
        id.ac.ui.cs.advprog.ordernotification.service.delivery.NotificationDeliveryStrategy noMatchStrategy =
                new id.ac.ui.cs.advprog.ordernotification.service.delivery.NotificationDeliveryStrategy() {
                    @Override
                    public boolean supports(String preferenceType) {
                        return false; // Never matches
                    }
                    @Override
                    public Notification deliver(String userId, String message, String type, String recipient) {
                        return null;
                    }
                };

        NotificationServiceImpl serviceWithNoMatch = new NotificationServiceImpl(
                repository, preferenceRepository, emailService, messagingTemplate,
                Optional.of(List.of(noMatchStrategy)));

        Order order = new Order();
        order.setId(1L);
        order.setUserId("user_fallback");
        order.setItemName("Test Item");

        NotificationPreference pref = new NotificationPreference();
        pref.setUserId("user_fallback");
        pref.setPushEnabled(true);

        when(preferenceRepository.findByUserId("user_fallback")).thenReturn(Optional.of(pref));

        serviceWithNoMatch.createOrderNotification(order);

        // The fallback builder should create a notification with status FAILED
        verify(repository, atLeastOnce()).save(argThat(n ->
                "FAILED".equals(n.getStatus()) && "PUSH".equals(n.getPreferenceType())));
    }

    @Test
    void testConstructor_WithEmptyStrategiesList() {
        // Test constructor when strategies list is empty
        NotificationServiceImpl serviceEmptyList = new NotificationServiceImpl(
                repository, preferenceRepository, emailService, messagingTemplate,
                Optional.of(java.util.Collections.emptyList()));

        // Verify service is created with default strategies
        assertNotNull(serviceEmptyList);
    }

    @Test
    void testSetPreference_ExistingPreference() {
        String userId = "user_existing";
        NotificationPreference existingPref = new NotificationPreference();
        existingPref.setUserId(userId);
        existingPref.setEmail("old@example.com");
        existingPref.setEmailEnabled(false);
        existingPref.setPushEnabled(false);

        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(existingPref));
        when(preferenceRepository.save(any(NotificationPreference.class))).thenAnswer(i -> i.getArguments()[0]);

        NotificationPreference result = service.setPreference(userId, "new@example.com", true, true);

        assertEquals(userId, result.getUserId());
        assertEquals("new@example.com", result.getEmail());
        assertTrue(result.isEmailEnabled());
        assertTrue(result.isPushEnabled());
    }

    @Test
    void testCreateWalletNotification_Topup() {
        String userId = "1";
        WalletNotificationEvent event = new WalletNotificationEvent(
                userId,
                "TOPUP",
                BigDecimal.valueOf(100000),
                "Top-up dari Bank BCA",
                LocalDateTime.parse("2026-05-20T18:35:00.123456"));

        NotificationPreference pref = new NotificationPreference();
        pref.setUserId(userId);
        pref.setPushEnabled(true);

        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(pref));

        service.createWalletNotification(event);

        verify(repository).save(argThat(n ->
                userId.equals(n.getUserId())
                        && "WALLET_TOPUP".equals(n.getType())
                        && "PUSH".equals(n.getPreferenceType())
                        && n.getMessage().contains("Rp100.000")
                        && n.getMessage().contains("Top-up dari Bank BCA")));
        verify(messagingTemplate).convertAndSend(
                eq("/topic/notifications/" + userId),
                contains("Top-up dari Bank BCA"));
    }

    @Test
    void testCreateWalletNotification_NullMessageIgnored() {
        service.createWalletNotification(null);

        verify(repository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), anyString());
    }

    @Test
    void testSendNotification_FeatureFlagPushDisabled() {
        FeatureFlagProperties flags = new FeatureFlagProperties();
        flags.setPushNotificationEnabled(false);
        NotificationServiceImpl pushDisabledService = new NotificationServiceImpl(
                repository,
                preferenceRepository,
                emailService,
                messagingTemplate,
                Optional.empty(),
                restTemplate,
                "",
                "token",
                flags);

        NotificationPreference pref = new NotificationPreference();
        pref.setUserId("user1");
        pref.setPushEnabled(true);

        when(preferenceRepository.findByUserId("user1")).thenReturn(Optional.of(pref));

        pushDisabledService.sendNotification("user1", "Message", "TYPE");

        verify(repository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), anyString());
    }

    @Test
    void testCreateWalletNotification_UserIdNullOrBlankIgnored() {
        service.createWalletNotification(new WalletNotificationEvent(
                null,
                "TOPUP",
                BigDecimal.TEN,
                "ignored",
                (String) null));
        service.createWalletNotification(new WalletNotificationEvent(
                " ",
                "TOPUP",
                BigDecimal.TEN,
                "ignored",
                (String) null));

        verify(repository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), anyString());
    }

    @Test
    void testCreateWalletNotification_BlankTypeNullAmountAndBlankDescriptionUsesDefaults() {
        String userId = "wallet-default";
        WalletNotificationEvent event = new WalletNotificationEvent(
                userId,
                " ",
                null,
                " ",
                (String) null);

        NotificationPreference pref = new NotificationPreference();
        pref.setUserId(userId);
        pref.setPushEnabled(true);

        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(pref));

        service.createWalletNotification(event);

        verify(repository).save(argThat(n ->
                userId.equals(n.getUserId())
                        && "WALLET".equals(n.getType())
                        && n.getMessage().contains("Rp0")
                        && !n.getMessage().contains(".  ")));
        verify(messagingTemplate).convertAndSend(
                eq("/topic/notifications/" + userId),
                eq("Wallet WALLET sebesar Rp0"));
    }

    @Test
    void testCreateWalletNotification_NullTypeAndNullDescriptionUsesDefaults() {
        String userId = "wallet-null-type";
        WalletNotificationEvent event = new WalletNotificationEvent(
                userId,
                null,
                BigDecimal.valueOf(5000),
                null,
                (String) null);

        NotificationPreference pref = new NotificationPreference();
        pref.setUserId(userId);
        pref.setPushEnabled(true);

        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(pref));

        service.createWalletNotification(event);

        verify(repository).save(argThat(n ->
                userId.equals(n.getUserId())
                        && "WALLET".equals(n.getType())
                        && "Wallet WALLET sebesar Rp5.000".equals(n.getMessage())));
    }

    @Test
    void testWalletNotificationEventWithNullLocalDateTimeKeepsTimestampNull() {
        WalletNotificationEvent event = new WalletNotificationEvent(
                "user1",
                "TOPUP",
                BigDecimal.TEN,
                "description",
                (LocalDateTime) null);

        assertNull(event.getTimestamp());
    }

    @Test
    void testCreateAuctionBidNotificationSendsToParticipantsAndSellerExceptBidder() {
        AuctionEventMessage event = new AuctionEventMessage(
                1L,
                "BidPlaced",
                LocalDateTime.now(),
                Map.of(
                        "auctionId", 10,
                        "sellerId", "seller1",
                        "itemName", "Laptop",
                        "bidderId", "bidder2",
                        "bidAmount", 150000
                ));

        NotificationPreference bidderPreference = new NotificationPreference();
        bidderPreference.setUserId("bidder1");
        bidderPreference.setPushEnabled(true);
        NotificationPreference sellerPreference = new NotificationPreference();
        sellerPreference.setUserId("seller1");
        sellerPreference.setPushEnabled(true);

        when(preferenceRepository.findByUserId("bidder1")).thenReturn(Optional.of(bidderPreference));
        when(preferenceRepository.findByUserId("seller1")).thenReturn(Optional.of(sellerPreference));
        when(auctionParticipantRepository.findByAuctionId(10L))
                .thenReturn(List.of(new AuctionParticipant(10L, "bidder1")));
        when(auctionParticipantRepository.findByAuctionIdAndUserId(10L, "bidder2"))
                .thenReturn(Optional.empty());
        when(auctionParticipantRepository.save(any(AuctionParticipant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.createAuctionBidNotification(event);

        verify(repository).save(argThat(n ->
                "bidder1".equals(n.getUserId()) && "AUCTION_BID_PLACED".equals(n.getType())));
        verify(repository).save(argThat(n ->
                "seller1".equals(n.getUserId()) && "AUCTION_BID_PLACED".equals(n.getType())));
        verify(repository, never()).save(argThat(n -> "bidder2".equals(n.getUserId())));
        verify(messagingTemplate).convertAndSend(eq("/topic/auctions/10"), eq(event));
        verify(auctionParticipantRepository).save(argThat(participant ->
                Long.valueOf(10L).equals(participant.getAuctionId())
                        && "bidder2".equals(participant.getUserId())));
    }

    @Test
    void testCreateAuctionOutbidNotification() {
        AuctionEventMessage event = new AuctionEventMessage(
                2L,
                "Outbid",
                LocalDateTime.now(),
                Map.of(
                        "auctionId", 11L,
                        "itemName", "Camera",
                        "bidderId", "oldWinner",
                        "newBidAmount", "250000"
                ));

        NotificationPreference preference = new NotificationPreference();
        preference.setUserId("oldWinner");
        preference.setPushEnabled(true);

        when(preferenceRepository.findByUserId("oldWinner")).thenReturn(Optional.of(preference));

        service.createAuctionOutbidNotification(event);

        verify(repository).save(argThat(n ->
                "oldWinner".equals(n.getUserId()) && "AUCTION_OUTBID".equals(n.getType())));
        verify(messagingTemplate).convertAndSend(eq("/topic/auctions/11"), eq(event));
    }

    @Test
    void testCreateAuctionWonNotification() {
        AuctionFinishedMessage message = new AuctionFinishedMessage(
                12L,
                "winner1",
                "seller1",
                "Keyboard",
                300000.0,
                5L);

        NotificationPreference preference = new NotificationPreference();
        preference.setUserId("winner1");
        preference.setPushEnabled(true);

        when(preferenceRepository.findByUserId("winner1")).thenReturn(Optional.of(preference));

        service.createAuctionWonNotification(message);

        verify(repository).save(argThat(n ->
                "winner1".equals(n.getUserId()) && "AUCTION_WON".equals(n.getType())));
    }

    @Test
    void testSendNotificationUsesEmailPreferenceWhenAvailable() {
        NotificationPreference preference = new NotificationPreference();
        preference.setUserId("winner1");
        preference.setEmail("winner@example.com");
        preference.setEmailEnabled(true);
        preference.setPushEnabled(true);

        when(preferenceRepository.findByUserId("winner1")).thenReturn(Optional.of(preference));

        service.sendNotification("winner1", "Anda memenangkan lelang.", "AUCTION_WON");

        verify(repository).save(argThat(n ->
                "winner1".equals(n.getUserId())
                        && "AUCTION_WON".equals(n.getType())
                        && "PUSH".equals(n.getPreferenceType())));
        verify(repository, atLeastOnce()).save(argThat(n ->
                "winner1".equals(n.getUserId())
                        && "AUCTION_WON".equals(n.getType())
                        && "EMAIL".equals(n.getPreferenceType())
                        && "winner@example.com".equals(n.getRecipientEmail())));
        verify(emailService).sendSimpleEmail(eq("winner@example.com"), contains("AUCTION_WON"), eq("Anda memenangkan lelang."));
    }

    @Test
    void testAuctionNotificationNullInputsAreIgnored() {
        service.createAuctionBidNotification(null);
        service.createAuctionOutbidNotification(new AuctionEventMessage());
        service.createAuctionWonNotification(null);
        service.createAuctionWonNotification(new AuctionFinishedMessage());

        verify(repository, never()).save(argThat(n -> n.getType() != null && n.getType().startsWith("AUCTION_")));
    }
}
