package id.ac.ui.cs.advprog.ordernotification.service.delivery;

import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import id.ac.ui.cs.advprog.ordernotification.repository.NotificationRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PushNotificationStrategy implements NotificationDeliveryStrategy {

    private final NotificationRepository repository;
    private final SimpMessagingTemplate messagingTemplate;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public PushNotificationStrategy(NotificationRepository repository, SimpMessagingTemplate messagingTemplate) {
        this.repository = repository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public boolean supports(String preferenceType) {
        return "PUSH".equalsIgnoreCase(preferenceType);
    }

    @Override
    public Notification deliver(String userId, String message, String type, String recipient) {
        Notification notif = new Notification();
        notif.setUserId(userId);
        notif.setMessage(message);
        notif.setType(type);
        notif.setPreferenceType("PUSH");
        notif.setStatus("SENT");
        notif.setRecipientEmail(null);
        notif.setCreatedAt(LocalDateTime.now());
        
        Notification savedNotif = repository.save(notif);
        
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, message);
        
        return savedNotif;
    }
}
