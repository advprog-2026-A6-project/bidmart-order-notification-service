package id.ac.ui.cs.advprog.ordernotification.feature;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NotificationServiceTest {

    @Autowired
    private NotificationService service;

    @MockBean
    private NotificationRepository repository;

    @Test
    void testGetAll() {
        Notification notif1 = new Notification("A", "INFO");
        Notification notif2 = new Notification("B", "WARN");

        when(repository.findAll()).thenReturn(List.of(notif1, notif2));

        List<Notification> result = service.getAll();

        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void testCreate() {
        Notification notif = new Notification("Hello", "INFO");

        when(repository.save(any(Notification.class))).thenReturn(notif);

        Notification result = service.create("Hello", "INFO");

        assertNotNull(result);
        assertEquals("Hello", result.getMessage());
        assertEquals("INFO", result.getType());

        verify(repository, times(1)).save(any(Notification.class));
    }
}