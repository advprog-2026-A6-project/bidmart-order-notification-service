package id.ac.ui.cs.advprog.ordernotification.feature;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService service;

    @Test
    void testGetAll() throws Exception {
        Notification notif = new Notification("Test", "INFO");

        when(service.getAll()).thenReturn(List.of(notif));

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk());
    }

    @Test
    void testCreate() throws Exception {
        Notification notif = new Notification("Hello", "INFO");

        when(service.create("Hello", "INFO")).thenReturn(notif);

        mockMvc.perform(post("/api/notifications")
                        .param("message", "Hello")
                        .param("type", "INFO"))
                .andExpect(status().isOk());
    }

    @Test
    void testViewPage() throws Exception {
        mockMvc.perform(get("/api/notifications/view"))
                .andExpect(status().isOk())
                .andExpect(view().name("notifications"));
    }
}