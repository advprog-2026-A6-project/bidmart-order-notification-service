package id.ac.ui.cs.advprog.ordernotification.service;

import id.ac.ui.cs.advprog.ordernotification.model.Order;
import id.ac.ui.cs.advprog.ordernotification.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class OrderServiceImplTest {

    private OrderRepository orderRepository;
    private NotificationService notificationService;
    private OrderServiceImpl service;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        notificationService = mock(NotificationService.class);
        service = new OrderServiceImpl(orderRepository, notificationService);
    }

    @Test
    void testCreateOrder() {
        Order order = new Order();
        order.setUserId("user1");
        order.setTotalPrice(100.0);

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setStatus("CREATED"); // 🔥 WAJIB

        when(orderRepository.save(order)).thenReturn(savedOrder);

        Order result = service.create(order);

        assertEquals("CREATED", result.getStatus());
        verify(notificationService).createOrderNotification(savedOrder);
    }

    @Test
    void testFindAll() {
        when(orderRepository.findAll()).thenReturn(java.util.List.of(new Order()));

        var result = service.findAll();

        assertEquals(1, result.size());
        verify(orderRepository).findAll();
    }
}