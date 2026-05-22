package id.ac.ui.cs.advprog.ordernotification.repository;

import id.ac.ui.cs.advprog.ordernotification.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByAuctionId(Long auctionId);
    List<Order> findByUserId(String userId);
}
