package id.ac.ui.cs.advprog.ordernotification.feature;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}