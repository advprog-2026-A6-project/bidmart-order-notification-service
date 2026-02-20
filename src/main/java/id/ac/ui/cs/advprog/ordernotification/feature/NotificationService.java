package id.ac.ui.cs.advprog.ordernotification.feature;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    public List<Notification> getAll() {
        return List.of(
                new Notification("You won the auction!", "WIN"),
                new Notification("New promo available", "INFO"),
                new Notification("Payment reminder", "ALERT")
        );
    }
}
