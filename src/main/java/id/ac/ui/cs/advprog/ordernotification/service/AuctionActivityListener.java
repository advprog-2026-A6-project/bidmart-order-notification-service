package id.ac.ui.cs.advprog.ordernotification.service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import id.ac.ui.cs.advprog.ordernotification.config.RabbitMQConfig;
import id.ac.ui.cs.advprog.ordernotification.model.AuctionEventMessage;
import id.ac.ui.cs.advprog.ordernotification.model.AuctionFinishedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuctionActivityListener {

    private final NotificationService notificationService;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public AuctionActivityListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.AUCTION_ACTIVITY_QUEUE_NAME)
    public void handleAuctionActivity(AuctionEventMessage event) {
        if (event == null || event.getEventType() == null) {
            return;
        }

        log.info("Received auction activity event: {}", event.getEventType());
        if ("BidPlaced".equals(event.getEventType())) {
            notificationService.createAuctionBidNotification(event);
        } else if ("Outbid".equals(event.getEventType())) {
            notificationService.createAuctionOutbidNotification(event);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.AUCTION_WON_QUEUE_NAME)
    public void handleAuctionWon(AuctionFinishedMessage message) {
        log.info("Received auction won event for auction {}", message == null ? null : message.getAuctionId());
        notificationService.createAuctionWonNotification(message);
    }
}
