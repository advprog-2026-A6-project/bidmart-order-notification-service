package id.ac.ui.cs.advprog.ordernotification.functional;

import id.ac.ui.cs.advprog.ordernotification.model.Order;
import id.ac.ui.cs.advprog.ordernotification.repository.NotificationRepository;
import id.ac.ui.cs.advprog.ordernotification.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Functional / Integration tests for the BidMart Order Notification Service.
 * These tests simulate real user flows end-to-end through the full Spring context
 * using an in-memory H2 database and MockMvc (headless browser simulation).
 */
@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:functionaldb",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop"
        }
)
@AutoConfigureMockMvc
class NotificationFunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockBean
    private ConnectionFactory connectionFactory;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        orderRepository.deleteAll();
    }

    // ===== Scenario 1: Full Page Navigation Functional Test =====

    @Test
    void testHomePage_ReturnsOrderListView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("orderList"));
    }

    @Test
    void testOrdersPage_ReturnsOrderListView() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orderList"));
    }

    @Test
    void testNotificationsPage_ReturnsView() throws Exception {
        mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk())
                .andExpect(view().name("notificationList"));
    }

    @Test
    void testPreferencesPage_ReturnsView() throws Exception {
        mockMvc.perform(get("/preferences"))
                .andExpect(status().isOk())
                .andExpect(view().name("preferences"));
    }

    @Test
    void testCreateOrderPage_ReturnsView() throws Exception {
        mockMvc.perform(get("/create-order"))
                .andExpect(status().isOk())
                .andExpect(view().name("createOrder"));
    }

    @Test
    void testSimulatePage_ReturnsView() throws Exception {
        mockMvc.perform(get("/simulate"))
                .andExpect(status().isOk())
                .andExpect(view().name("auctionFinish"));
    }

    // ===== Scenario 2: End-to-End Order Creation via REST API =====

    @Test
    void testAuctionFinishFlow_CreatesOrderAndNotification() throws Exception {
        // Step 1: Set user preferences (enable push, disable email)
        mockMvc.perform(post("/api/order-notification/preferences/winner123")
                        .param("email", "winner@example.com")
                        .param("emailEnabled", "false")
                        .param("pushEnabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("winner123"))
                .andExpect(jsonPath("$.pushEnabled").value(true));

        // Step 2: Simulate auction finish (creates order + notification)
        String payload = """
                {
                    "auctionId": 100,
                    "winnerId": "winner123",
                    "itemName": "Antique Vase",
                    "finalPrice": 5000000.0
                }
                """;

        mockMvc.perform(post("/api/order-notification/auction-finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("winner123"))
                .andExpect(jsonPath("$.itemName").value("Antique Vase"))
                .andExpect(jsonPath("$.status").value("PAID"));

        // Step 3: Verify order was persisted
        mockMvc.perform(get("/api/order-notification/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].itemName").value("Antique Vase"));

        // Step 4: Verify push notification was created for the user
        mockMvc.perform(get("/api/order-notification/notifications/winner123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].userId").value("winner123"))
                .andExpect(jsonPath("$[0].preferenceType").value("PUSH"));
    }

    // ===== Scenario 3: Full Order Lifecycle (Create → Ship → Confirm) =====

    @Test
    void testFullOrderLifecycle() throws Exception {
        // Step 1: Enable push for buyer
        mockMvc.perform(post("/api/order-notification/preferences/buyer001")
                        .param("pushEnabled", "true")
                        .param("emailEnabled", "false"))
                .andExpect(status().isOk());

        // Step 2: Create order via auction finish
        String payload = """
                {
                    "auctionId": 200,
                    "winnerId": "buyer001",
                    "itemName": "Rare Painting",
                    "finalPrice": 12000000.0
                }
                """;
        mockMvc.perform(post("/api/order-notification/auction-finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        // Get the created order ID
        Order createdOrder = orderRepository.findAll().get(0);
        Long orderId = createdOrder.getId();

        // Step 3: Seller updates tracking number → triggers ORDER_SHIPPED notification
        mockMvc.perform(post("/api/order-notification/orders/" + orderId + "/tracking")
                        .param("trackingNumber", "JNE-123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"))
                .andExpect(jsonPath("$.trackingNumber").value("JNE-123456789"));

        // Step 4: Buyer confirms receipt → triggers ORDER_COMPLETED notification
        mockMvc.perform(post("/api/order-notification/orders/" + orderId + "/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // Step 5: Verify multiple notifications were generated throughout the lifecycle
        mockMvc.perform(get("/api/order-notification/notifications/buyer001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3)))); // ORDER_CREATED + SHIPPED + COMPLETED
    }

    // ===== Scenario 4: Dispute Flow =====

    @Test
    void testDisputeFlow() throws Exception {
        // Setup preferences
        mockMvc.perform(post("/api/order-notification/preferences/disputeUser")
                        .param("pushEnabled", "true")
                        .param("emailEnabled", "false"))
                .andExpect(status().isOk());

        // Create order
        String payload = """
                {
                    "auctionId": 300,
                    "winnerId": "disputeUser",
                    "itemName": "Defective Item",
                    "finalPrice": 100000.0
                }
                """;
        mockMvc.perform(post("/api/order-notification/auction-finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        Order order = orderRepository.findAll().get(0);

        // Ship it
        mockMvc.perform(post("/api/order-notification/orders/" + order.getId() + "/tracking")
                        .param("trackingNumber", "SICEPAT-999"))
                .andExpect(status().isOk());

        // Submit dispute instead of confirming
        mockMvc.perform(post("/api/order-notification/orders/" + order.getId() + "/dispute")
                        .param("reason", "Barang rusak saat diterima"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISPUTED"))
                .andExpect(jsonPath("$.disputeReason").value("Barang rusak saat diterima"))
                .andExpect(jsonPath("$.disputeStatus").value("OPEN"));
    }

    // ===== Scenario 5: Notification Preference Management =====

    @Test
    void testPreferenceGetAndUpdate() throws Exception {
        // Get default preference (user doesn't exist yet → returns default)
        mockMvc.perform(get("/api/order-notification/preferences/newUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("newUser"));

        // Update preference
        mockMvc.perform(post("/api/order-notification/preferences/newUser")
                        .param("email", "newuser@bidmart.com")
                        .param("emailEnabled", "true")
                        .param("pushEnabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newuser@bidmart.com"))
                .andExpect(jsonPath("$.emailEnabled").value(true))
                .andExpect(jsonPath("$.pushEnabled").value(true));

        // Retrieve updated preference
        mockMvc.perform(get("/api/order-notification/preferences/newUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newuser@bidmart.com"));
    }

    // ===== Scenario 6: REST API for Notifications =====

    @Test
    void testCreateAndListNotificationsViaRestApi() throws Exception {
        // Create notification via REST
        mockMvc.perform(post("/api/notifications")
                        .param("message", "Test notification from functional test")
                        .param("type", "FUNCTIONAL_TEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Test notification from functional test"))
                .andExpect(jsonPath("$.type").value("FUNCTIONAL_TEST"));

        // List all notifications
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }
}
