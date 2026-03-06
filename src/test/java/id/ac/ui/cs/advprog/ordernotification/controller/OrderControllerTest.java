package id.ac.ui.cs.advprog.ordernotification.controller;

import id.ac.ui.cs.advprog.ordernotification.model.Order;
import id.ac.ui.cs.advprog.ordernotification.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService service;

    @Test
    void testGetOrders() throws Exception {

        Order order = new Order();
        order.setId(1L);
        order.setUserId("user1");
        order.setTotalPrice(100.0);

        when(service.findAll()).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("user1"));
    }

    @Test
    void testCreateOrder() throws Exception {

        Order saved = new Order();
        saved.setId(1L);
        saved.setUserId("user1");
        saved.setTotalPrice(100.0);

        when(service.create(org.mockito.Mockito.any(Order.class))).thenReturn(saved);

        mockMvc.perform(post("/api/orders")
                        .param("userId", "user1")
                        .param("totalPrice", "100.0"))
                .andExpect(status().isOk());

        verify(service).create(org.mockito.Mockito.any(Order.class));
    }
}