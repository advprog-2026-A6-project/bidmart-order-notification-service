package id.ac.ui.cs.advprog.ordernotification.service;

import id.ac.ui.cs.advprog.ordernotification.model.Order;
import id.ac.ui.cs.advprog.ordernotification.repository.OrderRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public OrderServiceImpl(OrderRepository orderRepository,
            NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
    }

    @Override
    public Order create(Order order) {
        if (order.getStatus() == null) {
            order.setStatus("CREATED");
        }
        Order savedOrder = orderRepository.save(order);
        notificationService.createOrderNotification(savedOrder);
        return savedOrder;
    }

    @Override
    public Order createAutomaticOrder(Long auctionId, String userId, String itemName, Double totalPrice) {
        Order order = new Order();
        order.setAuctionId(auctionId);
        order.setUserId(userId);
        order.setItemName(itemName);
        order.setTotalPrice(totalPrice);
        order.setStatus("AUTOMATIC_CREATED");

        Order savedOrder = orderRepository.save(order);
        notificationService.createOrderNotification(savedOrder);
        return savedOrder;
    }

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }
}