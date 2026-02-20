package id.ac.ui.cs.advprog.ordernotification.feature;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetAllNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateNotificationFlow() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .param("message", "Integration Test")
                        .param("type", "INFO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Integration Test"))
                .andExpect(jsonPath("$.type").value("INFO"));
    }

    @Test
    void testViewPage() throws Exception {
        mockMvc.perform(get("/api/notifications/view"))
                .andExpect(status().isOk())
                .andExpect(view().name("notifications"));
    }
}