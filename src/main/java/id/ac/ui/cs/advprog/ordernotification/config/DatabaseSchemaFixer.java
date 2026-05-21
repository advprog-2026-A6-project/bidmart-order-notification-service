package id.ac.ui.cs.advprog.ordernotification.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DatabaseSchemaFixer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private static final String ALTER_NOTIFICATIONS_MESSAGE_TO_TEXT =
            "ALTER TABLE notifications ALTER COLUMN message TYPE TEXT";
    private static final String ALTER_ORDERS_DISPUTE_REASON_TO_TEXT =
            "ALTER TABLE orders ALTER COLUMN dispute_reason TYPE TEXT";

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public DatabaseSchemaFixer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        alterNotificationsMessageToText();
        alterOrdersDisputeReasonToText();
    }

    private void alterNotificationsMessageToText() {
        try {
            jdbcTemplate.execute(ALTER_NOTIFICATIONS_MESSAGE_TO_TEXT);
            log.info("Successfully altered notifications.message column to TEXT");
        } catch (Exception e) {
            log.warn("Could not alter notifications.message: {}", e.getMessage());
        }
    }

    private void alterOrdersDisputeReasonToText() {
        try {
            jdbcTemplate.execute(ALTER_ORDERS_DISPUTE_REASON_TO_TEXT);
            log.info("Successfully altered orders.dispute_reason column to TEXT");
        } catch (Exception e) {
            log.warn("Could not alter orders.dispute_reason: {}", e.getMessage());
        }
    }
}
