package id.ac.ui.cs.advprog.ordernotification.service;

import id.ac.ui.cs.advprog.ordernotification.model.Order;
import id.ac.ui.cs.advprog.ordernotification.repository.OrderRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final String ORDER_NOT_FOUND_MSG = "Order not found";

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
        if (auctionId != null) {
            Optional<Order> existingOrder = orderRepository.findByAuctionId(auctionId);
            if (existingOrder.isPresent()) {
                return existingOrder.get();
            }
        }

        Order order = new Order();
        order.setAuctionId(auctionId);
        order.setUserId(userId);
        order.setItemName(itemName);
        order.setTotalPrice(totalPrice);
        order.setStatus("PAID");

        Order savedOrder = orderRepository.save(order);
        notificationService.createOrderNotification(savedOrder);
        return savedOrder;
    }

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Order findById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public List<Order> findByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public Order markPacked(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException(ORDER_NOT_FOUND_MSG));
        order.setStatus("PACKED");
        Order savedOrder = orderRepository.save(order);

        String message = "Pesanan #" + savedOrder.getId() + " (" + savedOrder.getItemName() + ") sedang dikemas.";
        notificationService.sendNotification(savedOrder.getUserId(), message, "ORDER_PACKED");

        return savedOrder;
    }

    @Override
    public Order updateTrackingNumber(Long id, String trackingNumber) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException(ORDER_NOT_FOUND_MSG));
        order.setTrackingNumber(trackingNumber);
        order.setStatus("SHIPPED");
        Order savedOrder = orderRepository.save(order);

        String message = "Pesanan #" + savedOrder.getId() + " (" + savedOrder.getItemName() + ") telah dikirim dengan nomor resi: " + trackingNumber;
        notificationService.sendNotification(savedOrder.getUserId(), message, "ORDER_SHIPPED");
        
        return savedOrder;
    }

    @Override
    public Order confirmReceipt(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException(ORDER_NOT_FOUND_MSG));
        order.setStatus("COMPLETED");
        Order savedOrder = orderRepository.save(order);

        String message = "Pesanan #" + savedOrder.getId() + " (" + savedOrder.getItemName() + ") telah selesai. Terima kasih!";
        notificationService.sendNotification(savedOrder.getUserId(), message, "ORDER_COMPLETED");
        
        return savedOrder;
    }

    @Override
    public Order submitDispute(Long id, String reason) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException(ORDER_NOT_FOUND_MSG));
        order.setDisputeReason(reason);
        order.setDisputeStatus("OPEN");
        order.setStatus("DISPUTED");
        Order savedOrder = orderRepository.save(order);
        
        String message = "Sengketa untuk pesanan #" + savedOrder.getId() + " (" + savedOrder.getItemName() + ") telah diajukan. Kami akan segera memprosesnya.";
        notificationService.sendNotification(savedOrder.getUserId(), message, "ORDER_DISPUTED");
        
        return savedOrder;
    }
}
