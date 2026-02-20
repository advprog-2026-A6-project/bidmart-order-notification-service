package id.ac.ui.cs.advprog.ordernotification.feature;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationControllerTest {

    @Mock
    private NotificationService service;

    @InjectMocks
    private NotificationController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAll() {
        Notification notif = new Notification("Test", "INFO");

        when(service.getAll()).thenReturn(List.of(notif));

        List<Notification> result = controller.getAll();

        assertEquals(1, result.size());
        assertEquals("Test", result.get(0).getMessage());

        verify(service).getAll();
    }

    @Test
    void testCreate() {
        Notification notif = new Notification("Hello", "INFO");

        when(service.create("Hello", "INFO")).thenReturn(notif);

        Notification result = controller.create("Hello", "INFO");

        assertEquals("Hello", result.getMessage());
        assertEquals("INFO", result.getType());

        verify(service).create("Hello", "INFO");
    }

    @Test
    void testViewPage() {
        String view = controller.viewPage();

        assertEquals("notifications", view);
    }
}