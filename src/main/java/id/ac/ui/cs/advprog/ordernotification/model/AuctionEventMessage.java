package id.ac.ui.cs.advprog.ordernotification.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class AuctionEventMessage {
    private Long eventId;
    private String eventType;
    private LocalDateTime occurredAt;
    private Map<String, Object> payload;

    public Long getAuctionId() {
        return longValue("auctionId");
    }

    public String getSellerId() {
        return stringValue("sellerId");
    }

    public String getItemName() {
        return stringValue("itemName");
    }

    public String getBidderId() {
        return stringValue("bidderId");
    }

    public String getNewBidderId() {
        return stringValue("newBidderId");
    }

    public BigDecimal getBidAmount() {
        return decimalValue("bidAmount");
    }

    public BigDecimal getNewBidAmount() {
        return decimalValue("newBidAmount");
    }

    public BigDecimal getCurrentHighestBid() {
        return decimalValue("currentHighestBid");
    }

    public List<String> getParticipantIds() {
        Object value = value("participantIds");
        if (value instanceof List<?> values) {
            return values.stream()
                    .filter(item -> item != null && !item.toString().isBlank())
                    .map(Object::toString)
                    .distinct()
                    .toList();
        }
        return List.of();
    }

    private Object value(String key) {
        if (payload == null) {
            return null;
        }
        return payload.get(key);
    }

    private String stringValue(String key) {
        Object value = value(key);
        return value == null ? null : value.toString();
    }

    private Long longValue(String key) {
        Object value = value(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value != null && !value.toString().isBlank()) {
            return Long.valueOf(value.toString());
        }
        return null;
    }

    private BigDecimal decimalValue(String key) {
        Object value = value(key);
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        if (value != null && !value.toString().isBlank()) {
            return new BigDecimal(value.toString());
        }
        return null;
    }
}
