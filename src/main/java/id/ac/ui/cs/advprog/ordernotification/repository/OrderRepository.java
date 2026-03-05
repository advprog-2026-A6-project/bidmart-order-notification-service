package id.ac.ui.cs.advprog.ordernotification.repository;

import id.ac.ui.cs.advprog.ordernotification.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}