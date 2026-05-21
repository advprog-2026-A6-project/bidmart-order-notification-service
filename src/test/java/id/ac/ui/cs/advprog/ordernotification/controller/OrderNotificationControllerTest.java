package id.ac.ui.cs.advprog.ordernotification.controller;

import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import id.ac.ui.cs.advprog.ordernotification.model.NotificationPreference;
import id.ac.ui.cs.advprog.ordernotification.service.NotificationService;
import id.ac.ui.cs.advprog.ordernotification.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderNotificationController.class)
class OrderNotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void testHandleAuctionFinish() throws Exception {
        mockMvc.perform(post("/api/order-notification/auction-finish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"auctionId\": 101, \"winnerId\": \"user123\", \"itemName\": \"M3 MacBook\", \"finalPrice\": 2500}"))
                .andExpect(status().isOk());

        verify(orderService).createAutomaticOrder(101L, "user123", "M3 MacBook", 2500.0);
    }

    @Test
    void testUpdatePreference() throws Exception {
        NotificationPreference pref = new NotificationPreference();
        pref.setUserId("user123");

        when(notificationService.setPreference("user123", "test@mail.com", true, true)).thenReturn(pref);

        mockMvc.perform(post("/api/order-notification/preferences/user123")
                .param("email", "test@mail.com")
                .param("emailEnabled", "true")
                .param("pushEnabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user123"));
    }

    @Test
    void testGetPreference() throws Exception {
        NotificationPreference pref = new NotificationPreference();
        pref.setUserId("user123");
        pref.setEmail("test@mail.com");

        when(notificationService.getPreference("user123")).thenReturn(pref);

        mockMvc.perform(get("/api/order-notification/preferences/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@mail.com"));
    }

    @Test
    void testGetUserNotifications() throws Exception {
        Notification notif = new Notification();
        notif.setMessage("Test Message");

        when(notificationService.findByUserId("user123")).thenReturn(List.of(notif));

        mockMvc.perform(get("/api/order-notification/notifications/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Test Message"));
    }

    @Test
    void testGetAllOrders() throws Exception {
        mockMvc.perform(get("/api/order-notification/orders"))
                .andExpect(status().isOk());

        verify(orderService).findAll();
    }

    @Test
    void testUpdateTracking() throws Exception {
        mockMvc.perform(post("/api/order-notification/orders/1/tracking")
                .param("trackingNumber", "RESI123"))
                .andExpect(status().isOk());

        verify(orderService).updateTrackingNumber(1L, "RESI123");
    }

    @Test
    void testConfirmReceipt() throws Exception {
        mockMvc.perform(post("/api/order-notification/orders/1/confirm"))
                .andExpect(status().isOk());

        verify(orderService).confirmReceipt(1L);
    }

    @Test
    void testSubmitDispute() throws Exception {
        mockMvc.perform(post("/api/order-notification/orders/1/dispute")
                .param("reason", "Barang palsu"))
                .andExpect(status().isOk());

        verify(orderService).submitDispute(1L, "Barang palsu");
    }

    @Test
    void testGetPreferenceForbidden() throws Exception {
        mockMvc.perform(get("/api/order-notification/preferences/user123")
                .header("X-User-Id", "otherUser"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetPreferenceAllowed() throws Exception {
        NotificationPreference pref = new NotificationPreference();
        pref.setUserId("user123");
        pref.setEmail("test@mail.com");

        when(notificationService.getPreference("user123")).thenReturn(pref);

        mockMvc.perform(get("/api/order-notification/preferences/user123")
                .header("X-User-Id", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@mail.com"));
    }

    @Test
    void testUpdatePreferenceForbidden() throws Exception {
        mockMvc.perform(post("/api/order-notification/preferences/user123")
                .header("X-User-Id", "otherUser")
                .param("email", "test@mail.com")
                .param("emailEnabled", "true")
                .param("pushEnabled", "true"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdatePreferenceAllowedWithMatchingHeader() throws Exception {
        NotificationPreference pref = new NotificationPreference();
        pref.setUserId("user123");

        when(notificationService.setPreference("user123", "test@mail.com", true, true)).thenReturn(pref);

        mockMvc.perform(post("/api/order-notification/preferences/user123")
                .header("X-User-Id", "user123")
                .param("email", "test@mail.com")
                .param("emailEnabled", "true")
                .param("pushEnabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user123"));
    }

    @Test
    void testGetUserNotificationsForbidden() throws Exception {
        mockMvc.perform(get("/api/order-notification/notifications/user123")
                .header("X-User-Id", "otherUser"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetUserNotificationsAllowedWithMatchingHeader() throws Exception {
        Notification notif = new Notification();
        notif.setMessage("Header Match");

        when(notificationService.findByUserId("user123")).thenReturn(List.of(notif));

        mockMvc.perform(get("/api/order-notification/notifications/user123")
                .header("X-User-Id", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Header Match"));
    }
}
