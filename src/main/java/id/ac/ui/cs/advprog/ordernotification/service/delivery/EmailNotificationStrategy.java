package id.ac.ui.cs.advprog.ordernotification.service.delivery;

import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import id.ac.ui.cs.advprog.ordernotification.repository.NotificationRepository;
import id.ac.ui.cs.advprog.ordernotification.service.EmailService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EmailNotificationStrategy implements NotificationDeliveryStrategy {

    private final NotificationRepository repository;
    private final EmailService emailService;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public EmailNotificationStrategy(NotificationRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    @Override
    public boolean supports(String preferenceType) {
        return "EMAIL".equalsIgnoreCase(preferenceType);
    }

    @Override
    public Notification deliver(String userId, String message, String type, String recipient) {
        Notification notif = new Notification();
        notif.setUserId(userId);
        notif.setMessage(message);
        notif.setType(type);
        notif.setPreferenceType("EMAIL");
        notif.setStatus("SENDING");
        notif.setRecipientEmail(recipient);
        notif.setCreatedAt(LocalDateTime.now());

        Notification savedNotif = repository.save(notif);

        String subject = "[BidMart] Notifikasi - " + type;
        if ("ORDER_CREATED".equalsIgnoreCase(type)) {
            subject = "[BidMart] Konfirmasi Pesanan Otomatis";
        } else if (type != null && type.startsWith("ORDER_")) {
            subject = "[BidMart] Pembaruan Pesanan - " + type.replace("ORDER_", "");
        }

        emailService.sendSimpleEmail(recipient, subject, message);

        savedNotif.setStatus("SENT");
        return repository.save(savedNotif);
    }
}
