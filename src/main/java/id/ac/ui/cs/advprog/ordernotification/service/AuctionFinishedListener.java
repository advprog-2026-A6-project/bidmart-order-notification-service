package id.ac.ui.cs.advprog.ordernotification.service;

import id.ac.ui.cs.advprog.ordernotification.config.RabbitMQConfig;
import id.ac.ui.cs.advprog.ordernotification.model.AuctionFinishedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuctionFinishedListener {

    private final OrderService orderService;

    public AuctionFinishedListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleAuctionFinished(AuctionFinishedMessage message) {
        log.info("Received RabbitMQ Message: Auction Finished for {}", message.getItemName());
        orderService.createAutomaticOrder(
                message.getAuctionId(),
                message.getWinnerId(),
                message.getItemName(),
                message.getFinalPrice()
        );
    }
}
