package id.ac.ui.cs.advprog.ordernotification.controller;

import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import id.ac.ui.cs.advprog.ordernotification.model.NotificationPreference;
import id.ac.ui.cs.advprog.ordernotification.model.Order;
import id.ac.ui.cs.advprog.ordernotification.service.NotificationService;
import id.ac.ui.cs.advprog.ordernotification.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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
    void testHandleAuctionFinishRejectsMissingPayloadField() throws Exception {
        mockMvc.perform(post("/api/order-notification/auction-finish")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"auctionId\": 101, \"winnerId\": \"user123\", \"finalPrice\": 2500}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Field 'itemName' is required"));
    }

    @Test
    void testHandleAuctionFinishRejectsInvalidNumericPayloadField() throws Exception {
        mockMvc.perform(post("/api/order-notification/auction-finish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"auctionId\": \"abc\", \"winnerId\": \"user123\", \"itemName\": \"M3 MacBook\", \"finalPrice\": 2500}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Field 'auctionId' must be a valid number"));
    }

    @Test
    void testHandleAuctionFinishAcceptsNumericPayloadAsString() throws Exception {
        mockMvc.perform(post("/api/order-notification/auction-finish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"auctionId\": \"101\", \"winnerId\": \"user123\", \"itemName\": \"M3 MacBook\", \"finalPrice\": \"2500\"}"))
                .andExpect(status().isOk());

        verify(orderService).createAutomaticOrder(101L, "user123", "M3 MacBook", 2500.0);
    }

    @Test
    void testSimulateAuctionWonCreatesNotificationAndOrder() throws Exception {
        Order order = new Order();
        order.setId(9L);
        order.setStatus("PAID");

        when(orderService.createAutomaticOrder(101L, "user123", "M3 MacBook", 2500.0)).thenReturn(order);

        mockMvc.perform(post("/api/order-notification/simulate/auction-won")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"auctionId": 101, "winnerId": "user123", "sellerId": "seller123", "itemName": "M3 MacBook", "finalPrice": 2500}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AUCTION_WON_NOTIFICATION_SENT_AND_ORDER_CREATED"))
                .andExpect(jsonPath("$.orderId").value(9))
                .andExpect(jsonPath("$.orderStatus").value("PAID"));

        verify(notificationService).createAuctionWonNotification(any());
        verify(orderService).createAutomaticOrder(101L, "user123", "M3 MacBook", 2500.0);
    }

    @Test
    void testSimulateAuctionWonCreatesOrderWithoutSellerId() throws Exception {
        Order order = new Order();
        order.setId(10L);
        order.setStatus("PAID");

        when(orderService.createAutomaticOrder(102L, "user456", "Camera", 4500.0)).thenReturn(order);

        mockMvc.perform(post("/api/order-notification/simulate/auction-won")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"auctionId": 102, "winnerId": "user456", "itemName": "Camera", "finalPrice": 4500}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(10));
    }

    @Test
    void testSimulateAuctionWonRejectsInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/order-notification/simulate/auction-won")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"auctionId": 101, "winnerId": "user123", "itemName": "M3 MacBook", "finalPrice": "oops"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Field 'finalPrice' must be a valid number"));
    }

    @Test
    void testSimulateBidPlaced() throws Exception {
        mockMvc.perform(post("/api/order-notification/simulate/bid-placed")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"auctionId": 101, "sellerId": "seller1", "bidderId": "bidder1", "itemName": "M3 MacBook", "bidAmount": 2500}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BID_PLACED_NOTIFICATION_SENT"));

        verify(notificationService).createAuctionBidNotification(any());
    }

    @Test
    void testSimulateOutbid() throws Exception {
        mockMvc.perform(post("/api/order-notification/simulate/outbid")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"auctionId": 101, "bidderId": "bidder1", "itemName": "M3 MacBook", "newBidAmount": 3000}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OUTBID_NOTIFICATION_SENT"));

        verify(notificationService).createAuctionOutbidNotification(any());
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
    void testGetOrderById() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setUserId("user123");
        order.setTrackingNumber("RESI123");

        when(orderService.findById(1L)).thenReturn(order);

        mockMvc.perform(get("/api/order-notification/orders/1")
                .header("X-User-Id", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingNumber").value("RESI123"));
    }

    @Test
    void testGetOrderByIdNotFound() throws Exception {
        when(orderService.findById(1L)).thenReturn(null);

        mockMvc.perform(get("/api/order-notification/orders/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetOrderByIdForbidden() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setUserId("user123");

        when(orderService.findById(1L)).thenReturn(order);

        mockMvc.perform(get("/api/order-notification/orders/1")
                .header("X-User-Id", "otherUser"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetUserOrders() throws Exception {
        Order order = new Order();
        order.setUserId("user123");
        order.setStatus("SHIPPED");

        when(orderService.findByUserId("user123")).thenReturn(List.of(order));

        mockMvc.perform(get("/api/order-notification/orders/user/user123")
                .header("X-User-Id", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SHIPPED"));
    }

    @Test
    void testGetUserOrdersForbidden() throws Exception {
        mockMvc.perform(get("/api/order-notification/orders/user/user123")
                .header("X-User-Id", "otherUser"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateTracking() throws Exception {
        mockMvc.perform(post("/api/order-notification/orders/1/tracking")
                .param("trackingNumber", "RESI123"))
                .andExpect(status().isOk());

        verify(orderService).updateTrackingNumber(1L, "RESI123");
    }

    @Test
    void testMarkPacked() throws Exception {
        mockMvc.perform(post("/api/order-notification/orders/1/packed"))
                .andExpect(status().isOk());

        verify(orderService).markPacked(1L);
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
