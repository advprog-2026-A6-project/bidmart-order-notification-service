package id.ac.ui.cs.advprog.ordernotification.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseSchemaFixerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private DatabaseSchemaFixer databaseSchemaFixer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        databaseSchemaFixer = new DatabaseSchemaFixer(jdbcTemplate);
    }

    @Test
    void testRunSuccess() throws Exception {
        // Both ALTER TABLE calls succeed
        doNothing().when(jdbcTemplate).execute("ALTER TABLE notifications ALTER COLUMN message TYPE TEXT");
        doNothing().when(jdbcTemplate).execute("ALTER TABLE orders ALTER COLUMN dispute_reason TYPE TEXT");

        assertDoesNotThrow(() -> databaseSchemaFixer.run());

        verify(jdbcTemplate).execute("ALTER TABLE notifications ALTER COLUMN message TYPE TEXT");
        verify(jdbcTemplate).execute("ALTER TABLE orders ALTER COLUMN dispute_reason TYPE TEXT");
    }

    @Test
    void testRunNotificationsAlterFails() throws Exception {
        // First ALTER TABLE fails, second succeeds
        doThrow(new RuntimeException("Column already TEXT"))
                .when(jdbcTemplate).execute("ALTER TABLE notifications ALTER COLUMN message TYPE TEXT");
        doNothing().when(jdbcTemplate).execute("ALTER TABLE orders ALTER COLUMN dispute_reason TYPE TEXT");

        assertDoesNotThrow(() -> databaseSchemaFixer.run());

        verify(jdbcTemplate).execute("ALTER TABLE notifications ALTER COLUMN message TYPE TEXT");
        verify(jdbcTemplate).execute("ALTER TABLE orders ALTER COLUMN dispute_reason TYPE TEXT");
    }

    @Test
    void testRunOrdersAlterFails() throws Exception {
        // First ALTER TABLE succeeds, second fails
        doNothing().when(jdbcTemplate).execute("ALTER TABLE notifications ALTER COLUMN message TYPE TEXT");
        doThrow(new RuntimeException("Column already TEXT"))
                .when(jdbcTemplate).execute("ALTER TABLE orders ALTER COLUMN dispute_reason TYPE TEXT");

        assertDoesNotThrow(() -> databaseSchemaFixer.run());

        verify(jdbcTemplate).execute("ALTER TABLE notifications ALTER COLUMN message TYPE TEXT");
        verify(jdbcTemplate).execute("ALTER TABLE orders ALTER COLUMN dispute_reason TYPE TEXT");
    }

    @Test
    void testRunBothAltersFail() throws Exception {
        // Both ALTER TABLE calls fail
        doThrow(new RuntimeException("Table not found"))
                .when(jdbcTemplate).execute("ALTER TABLE notifications ALTER COLUMN message TYPE TEXT");
        doThrow(new RuntimeException("Table not found"))
                .when(jdbcTemplate).execute("ALTER TABLE orders ALTER COLUMN dispute_reason TYPE TEXT");

        assertDoesNotThrow(() -> databaseSchemaFixer.run());

        verify(jdbcTemplate).execute("ALTER TABLE notifications ALTER COLUMN message TYPE TEXT");
        verify(jdbcTemplate).execute("ALTER TABLE orders ALTER COLUMN dispute_reason TYPE TEXT");
    }
}
