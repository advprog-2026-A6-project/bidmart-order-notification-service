package id.ac.ui.cs.advprog.ordernotification.controller;

import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import id.ac.ui.cs.advprog.ordernotification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService service;

    @Test
    void testGetNotifications() throws Exception {

        Notification notif = new Notification();
        notif.setId(1L);
        notif.setMessage("Test");
        notif.setType("ORDER");

        when(service.findAll()).thenReturn(List.of(notif));

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Test"));
    }

    @Test
    void testCreateNotification() throws Exception {

        Notification saved = new Notification();
        saved.setId(1L);
        saved.setMessage("Test");
        saved.setType("ORDER");

        when(service.create(org.mockito.Mockito.any(Notification.class))).thenReturn(saved);

        mockMvc.perform(post("/api/notifications")
                        .param("message", "Test")
                        .param("type", "ORDER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Test"));

        verify(service).create(org.mockito.Mockito.any(Notification.class));
    }
}