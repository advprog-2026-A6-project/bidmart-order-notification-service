package id.ac.ui.cs.advprog.ordernotification.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import id.ac.ui.cs.advprog.ordernotification.config.RabbitMQConfig;
import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import id.ac.ui.cs.advprog.ordernotification.repository.NotificationRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class AuthEventListener {

    private static final String STATUS_SENT = "SENT";
    private static final String SYSTEM_USER = "system";
    private static final String SYSTEM_PREFERENCE_TYPE = "SYSTEM";
    private static final String USER_ID_KEY = "userId";
    private static final String EMAIL_KEY = "email";
    private static final String ROLE_NAME_KEY = "roleName";
    private static final String UNKNOWN_VALUE = "UNKNOWN";
    private static final String ACCOUNT_DISABLED_EVENT = "auth.event.account_disabled";
    private static final String USER_ROLE_CHANGED_EVENT = "auth.event.user_role_changed";
    private static final String ROLE_CREATED_EVENT = "auth.event.role_created";
    private static final String PERMISSION_CREATED_EVENT = "auth.event.permission_created";
    private static final String ROLE_PERMISSION_CHANGED_EVENT = "auth.event.role_permission_changed";
    private static final String USER_NOTIFICATION_TOPIC_PREFIX = "/topic/notifications/";

    private final ObjectMapper objectMapper;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public AuthEventListener(ObjectMapper objectMapper, NotificationRepository notificationRepository,
                             SimpMessagingTemplate messagingTemplate) {
        this.objectMapper = objectMapper;
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.AUTH_QUEUE_NAME)
    public void handle(String payload, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        try {
            Map<String, Object> eventPayload = objectMapper.readValue(payload, new TypeReference<>() {
            });
            Notification notification = buildNotification(routingKey, eventPayload);
            notificationRepository.save(notification);
            publishLiveNotification(notification);
        } catch (Exception exception) {
            throw new IllegalStateException("Gagal memproses event auth: " + routingKey, exception);
        }
    }

    private void publishLiveNotification(Notification notification) {
        if (notification == null || SYSTEM_USER.equals(notification.getUserId())) {
            return;
        }

        messagingTemplate.convertAndSend(USER_NOTIFICATION_TOPIC_PREFIX + notification.getUserId(), notification);
    }

    private Notification buildNotification(String routingKey, Map<String, Object> payload) {
        Notification notification = new Notification();
        notification.setType(routingKey);
        notification.setStatus(STATUS_SENT);
        notification.setPreferenceType(SYSTEM_PREFERENCE_TYPE);
        notification.setCreatedAt(LocalDateTime.now());

        if (ACCOUNT_DISABLED_EVENT.equals(routingKey)) {
            notification.setUserId(stringValue(payload.get(USER_ID_KEY), SYSTEM_USER));
            notification.setRecipientEmail(stringValue(payload.get(EMAIL_KEY), null));
            notification.setMessage("Akun dinonaktifkan: " + stringValue(payload.get("reason"), "tanpa alasan"));
            return notification;
        }

        if (USER_ROLE_CHANGED_EVENT.equals(routingKey)) {
            notification.setUserId(stringValue(payload.get(USER_ID_KEY), SYSTEM_USER));
            notification.setRecipientEmail(stringValue(payload.get(EMAIL_KEY), null));
            notification.setMessage("Peran pengguna berubah: " + stringValue(payload.get("action"), "UPDATED")
                    + " " + stringValue(payload.get(ROLE_NAME_KEY), UNKNOWN_VALUE));
            return notification;
        }

        notification.setUserId(SYSTEM_USER);
        notification.setMessage(buildSystemMessage(routingKey, payload));
        return notification;
    }

    private String buildSystemMessage(String routingKey, Map<String, Object> payload) {
        return switch (routingKey) {
            case ROLE_CREATED_EVENT ->
                    "Role baru dibuat: " + stringValue(payload.get(ROLE_NAME_KEY), UNKNOWN_VALUE);
            case PERMISSION_CREATED_EVENT ->
                    "Permission baru dibuat: " + stringValue(payload.get("permissionName"), UNKNOWN_VALUE);
            case ROLE_PERMISSION_CHANGED_EVENT ->
                    "Permission role berubah: " + stringValue(payload.get("action"), "UPDATED")
                            + " " + stringValue(payload.get("permissionName"), UNKNOWN_VALUE)
                            + " pada role " + stringValue(payload.get(ROLE_NAME_KEY), UNKNOWN_VALUE);
            default -> "Auth event diterima";
        };
    }

    private String stringValue(Object value, String fallback) {
        return value == null ? fallback : value.toString();
    }
}
