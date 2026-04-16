package id.ac.ui.cs.advprog.ordernotification.service;

import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import id.ac.ui.cs.advprog.ordernotification.model.NotificationPreference;
import id.ac.ui.cs.advprog.ordernotification.model.Order;
import id.ac.ui.cs.advprog.ordernotification.repository.NotificationPreferenceRepository;
import id.ac.ui.cs.advprog.ordernotification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final EmailService emailService;

    public NotificationServiceImpl(NotificationRepository repository,
            NotificationPreferenceRepository preferenceRepository,
            EmailService emailService) {
        this.repository = repository;
        this.preferenceRepository = preferenceRepository;
        this.emailService = emailService;
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

    @Override
    public void createOrderNotification(Order order) {
        if (order == null || order.getId() == null) {
            return;
        }

        NotificationPreference pref = getPreference(order.getUserId());
        String messageBody = "Order #" + order.getId() + " untuk " + order.getItemName() + " telah dibuat.";

        if (pref.isPushEnabled()) {
            Notification notif = new Notification();
            notif.setUserId(order.getUserId());
            notif.setMessage(messageBody);
            notif.setType("ORDER_CREATED");
            notif.setPreferenceType("PUSH");
            notif.setStatus("SENT");
            repository.save(notif);
        }

        if (pref.getEmail() != null && !pref.getEmail().isEmpty() && pref.isEmailEnabled()) {
            Notification emailNotif = new Notification();
            emailNotif.setUserId(order.getUserId());
            emailNotif.setRecipientEmail(pref.getEmail());
            emailNotif.setMessage(messageBody);
            emailNotif.setType("ORDER_CREATED");
            emailNotif.setPreferenceType("EMAIL");
            emailNotif.setCreatedAt(java.time.LocalDateTime.now());
            emailNotif.setStatus("SENDING");
            repository.save(emailNotif);

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
            Notification errorNotif = new Notification();
            errorNotif.setUserId(order.getUserId());
            errorNotif.setMessage("Email GAGAL dikirim: Alamat email belum diatur.");
            errorNotif.setType("ORDER_CREATED");
            errorNotif.setPreferenceType("EMAIL_ERROR");
            errorNotif.setCreatedAt(java.time.LocalDateTime.now());
            errorNotif.setStatus("FAILED");
            repository.save(errorNotif);
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
                .orElseGet(() -> {
                    NotificationPreference defaultPref = new NotificationPreference();
                    defaultPref.setUserId(userId);
                    return defaultPref;
                });
    }
}