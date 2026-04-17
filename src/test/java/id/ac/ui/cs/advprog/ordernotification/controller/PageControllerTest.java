package id.ac.ui.cs.advprog.ordernotification.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PageController.class)
class PageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testOrdersPage() throws Exception {

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orderList"));
    }

    @Test
    void testCreateOrderPage() throws Exception {

        mockMvc.perform(get("/create-order"))
                .andExpect(status().isOk())
                .andExpect(view().name("createOrder"));
    }

    @Test
    void testNotificationsPage() throws Exception {

        mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk())
                .andExpect(view().name("notificationList"));
    }

    @Test
    void testHomePage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("orderList"));
    }

    @Test
    void testPreferencesPage() throws Exception {
        mockMvc.perform(get("/preferences"))
                .andExpect(status().isOk())
                .andExpect(view().name("preferences"));
    }

    @Test
    void testSimulatePage() throws Exception {
        mockMvc.perform(get("/simulate"))
                .andExpect(status().isOk())
                .andExpect(view().name("auctionFinish"));
    }
}