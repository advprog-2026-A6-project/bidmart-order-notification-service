package id.ac.ui.cs.advprog.ordernotification.service;

import id.ac.ui.cs.advprog.ordernotification.config.RabbitMQConfig;
import id.ac.ui.cs.advprog.ordernotification.model.AuctionFinishedMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class AuctionFinishedListener {

    private final OrderService orderService;

    public AuctionFinishedListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleAuctionFinished(AuctionFinishedMessage message) {
        System.out.println("Received RabbitMQ Message: Auction Finished for " + message.getItemName());
        orderService.createAutomaticOrder(
                message.getAuctionId(),
                message.getWinnerId(),
                message.getItemName(),
                message.getFinalPrice()
        );
    }
}
