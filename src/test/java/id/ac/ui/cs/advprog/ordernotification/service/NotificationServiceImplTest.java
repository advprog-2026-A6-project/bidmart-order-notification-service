package id.ac.ui.cs.advprog.ordernotification.service;

import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import id.ac.ui.cs.advprog.ordernotification.model.Order;
import id.ac.ui.cs.advprog.ordernotification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceImplTest {

    private NotificationRepository repository;
    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(NotificationRepository.class);
        service = new NotificationServiceImpl(repository);
    }

    @Test
    void testCreateOrderNotification() {
        Order order = new Order();
        order.setId(1L);

        service.createOrderNotification(order);

        verify(repository).save(any(Notification.class));
    }

    @Test
    void testCreate() {
        Notification notif = new Notification();
        notif.setMessage("Test");
        notif.setType("TYPE");

        when(repository.save(notif)).thenReturn(notif);

        Notification result = service.create(notif);

        assertEquals("Test", result.getMessage());
        verify(repository).save(notif);
    }

    @Test
    void testFindAll() {
        when(repository.findAll()).thenReturn(List.of(new Notification()));

        List<Notification> result = service.findAll();

        assertEquals(1, result.size());
        verify(repository).findAll();
    }
}