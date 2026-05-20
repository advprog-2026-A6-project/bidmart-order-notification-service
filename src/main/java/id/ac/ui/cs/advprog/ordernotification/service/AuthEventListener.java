package id.ac.ui.cs.advprog.ordernotification.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.ordernotification.config.RabbitMQConfig;
import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import id.ac.ui.cs.advprog.ordernotification.repository.NotificationRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class AuthEventListener {

    private final ObjectMapper objectMapper;
    private final NotificationRepository notificationRepository;

    public AuthEventListener(ObjectMapper objectMapper, NotificationRepository notificationRepository) {
        this.objectMapper = objectMapper;
        this.notificationRepository = notificationRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.AUTH_QUEUE_NAME)
    public void handle(String payload, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        try {
            Map<String, Object> eventPayload = objectMapper.readValue(payload, new TypeReference<>() {
            });
            notificationRepository.save(buildNotification(routingKey, eventPayload));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to process auth event: " + routingKey, exception);
        }
    }

    private Notification buildNotification(String routingKey, Map<String, Object> payload) {
        Notification notification = new Notification();
        notification.setType(routingKey);
        notification.setStatus("SENT");
        notification.setPreferenceType("SYSTEM");
        notification.setCreatedAt(LocalDateTime.now());

        if ("auth.event.account_disabled".equals(routingKey)) {
            notification.setUserId(stringValue(payload.get("userId"), "system"));
            notification.setRecipientEmail(stringValue(payload.get("email"), null));
            notification.setMessage("Akun dinonaktifkan: " + stringValue(payload.get("reason"), "tanpa alasan"));
            return notification;
        }

        if ("auth.event.user_role_changed".equals(routingKey)) {
            notification.setUserId(stringValue(payload.get("userId"), "system"));
            notification.setRecipientEmail(stringValue(payload.get("email"), null));
            notification.setMessage("Peran pengguna berubah: " + stringValue(payload.get("action"), "UPDATED")
                    + " " + stringValue(payload.get("roleName"), "UNKNOWN"));
            return notification;
        }

        notification.setUserId("system");
        notification.setMessage(switch (routingKey) {
            case "auth.event.role_created" ->
                    "Role baru dibuat: " + stringValue(payload.get("roleName"), "UNKNOWN");
            case "auth.event.permission_created" ->
                    "Permission baru dibuat: " + stringValue(payload.get("permissionName"), "UNKNOWN");
            case "auth.event.role_permission_changed" ->
                    "Permission role berubah: " + stringValue(payload.get("action"), "UPDATED")
                            + " " + stringValue(payload.get("permissionName"), "UNKNOWN")
                            + " pada role " + stringValue(payload.get("roleName"), "UNKNOWN");
            default -> "Auth event diterima";
        });
        return notification;
    }

    private String stringValue(Object value, String fallback) {
        return value == null ? fallback : value.toString();
    }
}
