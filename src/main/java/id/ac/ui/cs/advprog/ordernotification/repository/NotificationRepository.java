package id.ac.ui.cs.advprog.ordernotification.repository;

import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(String userId);
}