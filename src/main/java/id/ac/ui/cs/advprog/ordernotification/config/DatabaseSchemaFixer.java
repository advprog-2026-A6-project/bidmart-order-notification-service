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

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public DatabaseSchemaFixer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        alterColumnToText("notifications", "message");
        alterColumnToText("orders", "dispute_reason");
    }

    private void alterColumnToText(String tableName, String columnName) {
        try {
            jdbcTemplate.execute("ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " TYPE TEXT");
            log.info("Successfully altered {}.{} column to TEXT", tableName, columnName);
        } catch (Exception e) {
            log.warn("Could not alter {}.{}: {}", tableName, columnName, e.getMessage());
        }
    }
}
