package id.ac.ui.cs.advprog.ordernotification.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "notifications", indexes = {@Index(name = "idx_notifications_user_id", columnList = "userId")})
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String recipientEmail;
    @Column(columnDefinition = "TEXT")
    private String message;
    private String type;
    private String status;
    private String preferenceType;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public static class Builder {
        private String userId;
        private String recipientEmail;
        private String message;
        private String type;
        private String status;
        private String preferenceType;
        private LocalDateTime createdAt = LocalDateTime.now();

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder recipientEmail(String recipientEmail) {
            this.recipientEmail = recipientEmail;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder preferenceType(String preferenceType) {
            this.preferenceType = preferenceType;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Notification build() {
            Notification notification = new Notification();
            notification.setUserId(this.userId);
            notification.setRecipientEmail(this.recipientEmail);
            notification.setMessage(this.message);
            notification.setType(this.type);
            notification.setStatus(this.status);
            notification.setPreferenceType(this.preferenceType);
            notification.setCreatedAt(this.createdAt);
            return notification;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}