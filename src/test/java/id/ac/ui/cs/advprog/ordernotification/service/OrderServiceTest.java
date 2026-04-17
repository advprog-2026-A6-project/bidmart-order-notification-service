package id.ac.ui.cs.advprog.ordernotification.service;

import id.ac.ui.cs.advprog.ordernotification.model.Order;
import id.ac.ui.cs.advprog.ordernotification.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateAutomaticOrder() {
        Long auctionId = 1L;
        String userId = "user123";
        String itemName = "Item A";
        Double price = 100.0;

        Order mockOrder = new Order();
        mockOrder.setId(1L);
        mockOrder.setAuctionId(auctionId);
        mockOrder.setUserId(userId);
        mockOrder.setItemName(itemName);
        mockOrder.setTotalPrice(price);
        mockOrder.setStatus("AUTOMATIC_CREATED");

        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        Order result = orderService.createAutomaticOrder(auctionId, userId, itemName, price);

        assertNotNull(result);
        assertEquals(auctionId, result.getAuctionId());
        assertEquals(userId, result.getUserId());
        assertEquals("AUTOMATIC_CREATED", result.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(notificationService, times(1)).createOrderNotification(any(Order.class));
    }
}
