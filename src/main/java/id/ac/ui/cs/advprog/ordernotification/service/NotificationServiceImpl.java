package id.ac.ui.cs.advprog.ordernotification.service;

import id.ac.ui.cs.advprog.ordernotification.dto.AuthContactPreferencesDto;
import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import id.ac.ui.cs.advprog.ordernotification.model.NotificationPreference;
import id.ac.ui.cs.advprog.ordernotification.model.Order;
import id.ac.ui.cs.advprog.ordernotification.repository.NotificationPreferenceRepository;
import id.ac.ui.cs.advprog.ordernotification.repository.NotificationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RestTemplate restTemplate;
    private final String authServiceUrl;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public NotificationServiceImpl(NotificationRepository repository,
            NotificationPreferenceRepository preferenceRepository,
            EmailService emailService,
            SimpMessagingTemplate messagingTemplate,
            RestTemplate restTemplate,
            @Value("${service.auth.url:http://35.168.202.46:8081/api/internal/users/}") String authServiceUrl) {
        this.repository = repository;
        this.preferenceRepository = preferenceRepository;
        this.emailService = emailService;
        this.messagingTemplate = messagingTemplate;
        this.restTemplate = restTemplate;
        this.authServiceUrl = authServiceUrl;
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
        return repository.findAll().stream()
                .filter(n -> userId.equals(n.getUserId()))
                .toList();
    }

    private Notification saveNotification(String userId, String message, String type, 
                                          String prefType, String status, String recipientEmail) {
        Notification notif = new Notification();
        notif.setUserId(userId);
        notif.setMessage(message);
        notif.setType(type);
        notif.setPreferenceType(prefType);
        notif.setStatus(status);
        notif.setRecipientEmail(recipientEmail);
        notif.setCreatedAt(java.time.LocalDateTime.now());
        return repository.save(notif);
    }

    private static final String NOTIF_TYPE_ORDER_CREATED = "ORDER_CREATED";

    @Override
    public void createOrderNotification(Order order) {
        if (order == null || order.getId() == null) {
            return;
        }

        NotificationPreference pref = getPreference(order.getUserId());
        String messageBody = "Order #" + order.getId() + " untuk " + order.getItemName() + " telah dibuat.";

        if (pref.isPushEnabled()) {
            saveNotification(order.getUserId(), messageBody, NOTIF_TYPE_ORDER_CREATED, "PUSH", "SENT", null);
            // Send WebSocket notification
            messagingTemplate.convertAndSend("/topic/notifications/" + order.getUserId(), messageBody);
        }

        if (pref.getEmail() != null && !pref.getEmail().isEmpty() && pref.isEmailEnabled()) {
            Notification emailNotif = saveNotification(order.getUserId(), messageBody, NOTIF_TYPE_ORDER_CREATED, 
                                                       "EMAIL", "SENDING", pref.getEmail());

            emailService.sendSimpleEmail(
                    pref.getEmail(),
                    "[BidMart] Konfirmasi Pesanan Otomatis - #" + order.getId(),
                    "Yth. Pengguna BidMart,\n\n" +
                            "Selamat! Anda telah memenangkan lelang dan pesanan Anda telah berhasil dibuat secara otomatis melalui sistem BidMart.\n\n"
                            +
                            "Detail Pesanan:\n" +
                            "- ID Pesanan: #" + order.getId() + "\n" +
                            "- Nama Barang: " + order.getItemName() + "\n" +
                            "- Total Harga: Rp" + String.format("%,.0f", order.getTotalPrice()) + "\n\n" +
                            "Status: SEDANG DIPROSES\n\n" +
                            "Terima kasih telah bertransaksi di BidMart. Jika Anda memiliki pertanyaan, silakan hubungi tim bantuan kami.\n\n"
                            +
                            "Salam hangat,\n" +
                            "BidMart");

            emailNotif.setStatus("SENT");
            repository.save(emailNotif);
        } else if (pref.isEmailEnabled()) {
            saveNotification(order.getUserId(), "Email GAGAL dikirim: Alamat email belum diatur.", 
                             NOTIF_TYPE_ORDER_CREATED, "EMAIL_ERROR", "FAILED", null);
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
        if (pref.isPushEnabled()) {
            saveNotification(userId, message, type, "PUSH", "SENT", null);
            messagingTemplate.convertAndSend("/topic/notifications/" + userId, message);
        }
    }

    private java.util.Optional<NotificationPreference> fetchPreferenceFromAuth(String userId) {
        try {
            AuthContactPreferencesDto authPreference = restTemplate.getForObject(
                    authServiceUrl + userId + "/contact-preferences",
                    AuthContactPreferencesDto.class
            );

            if (authPreference == null) {
                return java.util.Optional.empty();
            }

            NotificationPreference preference = new NotificationPreference();
            preference.setUserId(userId);
            preference.setEmail(authPreference.getEmail());
            preference.setEmailEnabled(authPreference.isEmailNotificationsEnabled());
            preference.setPushEnabled(authPreference.isPushNotificationsEnabled());
            return java.util.Optional.of(preference);
        } catch (RestClientException exception) {
            return java.util.Optional.empty();
        }
    }
}
