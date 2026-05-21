package id.ac.ui.cs.advprog.ordernotification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WalletNotificationEvent {
    private String userId;
    private String type;
    private BigDecimal amount;
    private String description;
    private String timestamp;

    public WalletNotificationEvent(String userId, String type, BigDecimal amount,
            String description, LocalDateTime timestamp) {
        this(userId, type, amount, description, timestamp == null ? null : timestamp.toString());
    }
}
