package id.ac.ui.cs.advprog.ordernotification.service;

import id.ac.ui.cs.advprog.ordernotification.config.FeatureFlagProperties;
import id.ac.ui.cs.advprog.ordernotification.dto.AuthContactPreferencesDto;
import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import id.ac.ui.cs.advprog.ordernotification.model.NotificationFactory;
import id.ac.ui.cs.advprog.ordernotification.model.NotificationPreference;
import id.ac.ui.cs.advprog.ordernotification.model.Order;
import id.ac.ui.cs.advprog.ordernotification.event.WalletNotificationEvent;
import id.ac.ui.cs.advprog.ordernotification.repository.NotificationPreferenceRepository;
import id.ac.ui.cs.advprog.ordernotification.repository.NotificationRepository;
import id.ac.ui.cs.advprog.ordernotification.service.delivery.NotificationDeliveryStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.Locale;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final List<NotificationDeliveryStrategy> deliveryStrategies;
    private final RestTemplate restTemplate;
    private final String authServiceUrl;
    private final String authInternalToken;
    private final FeatureFlagProperties featureFlags;

    private static final String NOTIF_TYPE_ORDER_CREATED = "ORDER_CREATED";
    private static final String DEFAULT_WALLET_TYPE = "WALLET";

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    @Autowired
    public NotificationServiceImpl(NotificationRepository repository,
            NotificationPreferenceRepository preferenceRepository,
            EmailService emailService,
            SimpMessagingTemplate messagingTemplate,
            Optional<List<NotificationDeliveryStrategy>> deliveryStrategiesOpt,
            RestTemplate restTemplate,
            @Value("${service.auth.url:}") String authServiceUrl,
            @Value("${service.auth.internal-token:${AUTH_INTERNAL_SERVICE_TOKEN:bidmart-internal-dev-token}}")
            String authInternalToken,
            FeatureFlagProperties featureFlags) {
        this.repository = repository;
        this.preferenceRepository = preferenceRepository;
        this.restTemplate = restTemplate;
        this.authServiceUrl = authServiceUrl;
        this.authInternalToken = authInternalToken;
        this.featureFlags = featureFlags;
        
        List<NotificationDeliveryStrategy> strategies = deliveryStrategiesOpt.isPresent()
                ? deliveryStrategiesOpt.get() : Collections.emptyList();
        if (strategies.isEmpty()) {
            this.deliveryStrategies = List.of(
                new id.ac.ui.cs.advprog.ordernotification.service.delivery.PushNotificationStrategy(repository, messagingTemplate),
                new id.ac.ui.cs.advprog.ordernotification.service.delivery.EmailNotificationStrategy(repository, emailService)
            );
        } else {
            this.deliveryStrategies = strategies;
        }
    }

    public NotificationServiceImpl(NotificationRepository repository,
            NotificationPreferenceRepository preferenceRepository,
            EmailService emailService,
            SimpMessagingTemplate messagingTemplate,
            Optional<List<NotificationDeliveryStrategy>> deliveryStrategiesOpt) {
        this(repository, preferenceRepository, emailService, messagingTemplate, deliveryStrategiesOpt,
                null, "", "bidmart-internal-dev-token", new FeatureFlagProperties());
    }

    @Override
    public Notification create(Notification notification) {
        if (notification.getStatus() == null) {
            notification.setStatus("PENDING");
        }
        return repository.save(notification);
    }

    @Override
    public List<Notification> findAll() {
        return repository.findAll();
    }

    @Override
    public List<Notification> findByUserId(String userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public void createOrderNotification(Order order) {
        if (order == null || order.getId() == null) {
            return;
        }

        NotificationPreference pref = getPreference(order.getUserId());
        String messageBody = "Order #" + order.getId() + " untuk " + order.getItemName() + " telah dibuat.";

        if (featureFlags.isPushNotificationEnabled() && pref.isPushEnabled()) {
            executeDelivery("PUSH", order.getUserId(), messageBody, NOTIF_TYPE_ORDER_CREATED, null);
        }

        if (featureFlags.isEmailNotificationEnabled() && pref.getEmail() != null && !pref.getEmail().isEmpty() && pref.isEmailEnabled()) {
            String emailContent = "Yth. Pengguna BidMart,\n\n" +
                    "Selamat! Anda telah memenangkan lelang dan pesanan Anda telah berhasil dibuat secara otomatis melalui sistem BidMart.\n\n" +
                    "Detail Pesanan:\n" +
                    "- ID Pesanan: #" + order.getId() + "\n" +
                    "- Nama Barang: " + order.getItemName() + "\n" +
                    "- Total Harga: Rp" + String.format("%,.0f", order.getTotalPrice()) + "\n\n" +
                    "Status: SEDANG DIPROSES\n\n" +
                    "Terima kasih telah bertransaksi di BidMart. Jika Anda memiliki pertanyaan, silakan hubungi tim bantuan kami.\n\n" +
                    "Salam hangat,\n" +
                    "BidMart";
            executeDelivery("EMAIL", order.getUserId(), emailContent, NOTIF_TYPE_ORDER_CREATED, pref.getEmail());
        } else if (pref.isEmailEnabled()) {
            Notification emailErrorNotif = NotificationFactory.createEmailErrorNotification(
                    order.getUserId(), 
                    "Email GAGAL dikirim: Alamat email belum diatur.", 
                    NOTIF_TYPE_ORDER_CREATED
            );
            repository.save(emailErrorNotif);
        }
    }

    @Override
    public NotificationPreference setPreference(String userId, String email, boolean emailEnabled,
            boolean pushEnabled) {
        NotificationPreference pref = preferenceRepository.findByUserId(userId)
                .orElse(new NotificationPreference());
        pref.setUserId(userId);
        pref.setEmail(email);
        pref.setEmailEnabled(emailEnabled);
        pref.setPushEnabled(pushEnabled);
        return preferenceRepository.save(pref);
    }

    @Override
    public NotificationPreference getPreference(String userId) {
        return preferenceRepository.findByUserId(userId)
                .orElseGet(() -> fetchPreferenceFromAuth(userId)
                        .map(preferenceRepository::save)
                        .orElseGet(() -> {
                            NotificationPreference defaultPref = new NotificationPreference();
                            defaultPref.setUserId(userId);
                            return defaultPref;
                        }));
    }

    @Override
    public void sendNotification(String userId, String message, String type) {
        NotificationPreference pref = getPreference(userId);
        if (featureFlags.isPushNotificationEnabled() && pref.isPushEnabled()) {
            executeDelivery("PUSH", userId, message, type, null);
        }
    }

    @Override
    public void createWalletNotification(WalletNotificationEvent event) {
        if (event == null || event.getUserId() == null || event.getUserId().isBlank()) {
            return;
        }

        String type = normalizeWalletType(event.getType());
        String notificationMessage = buildWalletMessage(event, type);
        sendNotification(event.getUserId(), notificationMessage, type);
    }

    private String normalizeWalletType(String type) {
        if (type == null || type.isBlank()) {
            return DEFAULT_WALLET_TYPE;
        }
        return "WALLET_" + type.trim().toUpperCase(Locale.ROOT);
    }

    private String buildWalletMessage(WalletNotificationEvent event, String type) {
        String action = type.replace("WALLET_", "").replace('_', ' ');
        StringBuilder builder = new StringBuilder("Wallet ")
                .append(action)
                .append(" sebesar ")
                .append(formatRupiah(event.getAmount()));

        if (event.getDescription() != null && !event.getDescription().isBlank()) {
            builder.append(". ").append(event.getDescription().trim());
        }

        return builder.toString();
    }

    private String formatRupiah(BigDecimal amount) {
        BigDecimal safeAmount = amount == null ? BigDecimal.ZERO : amount;
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("id-ID"));
        symbols.setGroupingSeparator('.');
        DecimalFormat format = new DecimalFormat("'Rp'#,##0", symbols);
        return format.format(safeAmount);
    }

    private void executeDelivery(String channel, String userId, String message, String type, String recipient) {
        for (NotificationDeliveryStrategy strategy : deliveryStrategies) {
            if (strategy.supports(channel)) {
                strategy.deliver(userId, message, type, recipient);
                return;
            }
        }

        saveFailedDelivery(userId, message, type, channel);
    }

    private void saveFailedDelivery(String userId, String message, String type, String channel) {
        Notification fallbackNotif = Notification.builder()
                .userId(userId)
                .message(message)
                .type(type)
                .preferenceType(channel)
                .status("FAILED")
                .createdAt(java.time.LocalDateTime.now())
                .build();
        repository.save(fallbackNotif);
    }

    private Optional<NotificationPreference> fetchPreferenceFromAuth(String userId) {
        if (restTemplate == null || authServiceUrl == null || authServiceUrl.isBlank()) {
            return Optional.empty();
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            if (authInternalToken != null && !authInternalToken.isBlank()) {
                headers.set("X-Internal-Service-Token", authInternalToken);
            }

            ResponseEntity<AuthContactPreferencesDto> response = restTemplate.exchange(
                    authServiceUrl + userId + "/contact-preferences",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    AuthContactPreferencesDto.class
            );
            AuthContactPreferencesDto authPreference = response.getBody();

            if (authPreference == null) {
                return Optional.empty();
            }

            NotificationPreference preference = new NotificationPreference();
            preference.setUserId(userId);
            preference.setEmail(authPreference.getEmail());
            preference.setEmailEnabled(authPreference.isEmailNotificationsEnabled());
            preference.setPushEnabled(authPreference.isPushNotificationsEnabled());
            return Optional.of(preference);
        } catch (RestClientException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
