package id.ac.ui.cs.advprog.ordernotification.service;

import id.ac.ui.cs.advprog.ordernotification.model.Order;
import id.ac.ui.cs.advprog.ordernotification.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
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
        savedOrder.setStatus("CREATED");

        when(orderRepository.save(order)).thenReturn(savedOrder);

        Order result = service.create(order);

        assertEquals("CREATED", result.getStatus());
        verify(notificationService).createOrderNotification(savedOrder);
    }

    @Test
    void testCreateOrder_WithExistingStatus() {
        Order order = new Order();
        order.setStatus("PRE_EXISTING");

        Order savedOrder = new Order();
        savedOrder.setStatus("PRE_EXISTING");

        when(orderRepository.save(order)).thenReturn(savedOrder);

        Order result = service.create(order);

        assertEquals("PRE_EXISTING", result.getStatus());
        verify(notificationService).createOrderNotification(savedOrder);
    }

    @Test
    void testFindAll() {
        when(orderRepository.findAll()).thenReturn(java.util.List.of(new Order()));

        var result = service.findAll();

        assertEquals(1, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    void testFindById() {
        Order order = new Order();
        order.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(order));

        Order result = service.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testFindById_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        Order result = service.findById(1L);

        assertNull(result);
    }

    @Test
    void testUpdateTrackingNumber() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId("user1");
        order.setItemName("Item A");

        when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        Order result = service.updateTrackingNumber(1L, "RESI123");

        assertEquals("RESI123", result.getTrackingNumber());
        assertEquals("SHIPPED", result.getStatus());
        verify(notificationService).sendNotification(eq("user1"), anyString(), eq("ORDER_SHIPPED"));
    }

    @Test
    void testUpdateTrackingNumber_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> service.updateTrackingNumber(1L, "RESI123"));
    }

    @Test
    void testConfirmReceipt() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId("user1");
        order.setItemName("Item A");

        when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        Order result = service.confirmReceipt(1L);

        assertEquals("COMPLETED", result.getStatus());
        verify(notificationService).sendNotification(eq("user1"), anyString(), eq("ORDER_COMPLETED"));
    }

    @Test
    void testConfirmReceipt_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> service.confirmReceipt(1L));
    }

    @Test
    void testSubmitDispute() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId("user1");
        order.setItemName("Item A");

        when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        Order result = service.submitDispute(1L, "Barang rusak");

        assertEquals("DISPUTED", result.getStatus());
        assertEquals("OPEN", result.getDisputeStatus());
        assertEquals("Barang rusak", result.getDisputeReason());
        verify(notificationService).sendNotification(eq("user1"), anyString(), eq("ORDER_DISPUTED"));
    }

    @Test
    void testSubmitDispute_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> service.submitDispute(1L, "Barang rusak"));
    }
}