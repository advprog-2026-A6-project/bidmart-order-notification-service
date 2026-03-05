package id.ac.ui.cs.advprog.ordernotification.repository;

import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository repository;

    @Test
    void testSaveNotification() {
        Notification notif = new Notification();
        notif.setMessage("Test");
        notif.setType("ORDER");

        Notification saved = repository.save(notif);

        assertNotNull(saved.getId());
    }
}