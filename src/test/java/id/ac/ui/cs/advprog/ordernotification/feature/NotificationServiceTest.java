package id.ac.ui.cs.advprog.ordernotification.feature;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private NotificationService service;

    @Test
    void testGetAll() {
        Notification notif = new Notification("A", "INFO");

        when(repository.findAll()).thenReturn(List.of(notif));

        List<Notification> result = service.getAll();

        assertEquals(1, result.size());
        verify(repository).findAll();
    }

    @Test
    void testCreate() {
        Notification notif = new Notification("Hello", "INFO");

        when(repository.save(any())).thenReturn(notif);

        Notification result = service.create("Hello", "INFO");

        assertEquals("Hello", result.getMessage());
        assertEquals("INFO", result.getType());
        verify(repository).save(any());
    }
}