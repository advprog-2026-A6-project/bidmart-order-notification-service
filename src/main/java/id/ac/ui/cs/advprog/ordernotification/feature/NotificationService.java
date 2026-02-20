package id.ac.ui.cs.advprog.ordernotification.feature;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    public List<Notification> getAll() {
        return repository.findAll();
    }

    public Notification create(String message, String type) {
        Notification notif = new Notification(message, type);
        return repository.save(notif);
    }
}