package id.ac.ui.cs.advprog.ordernotification.service;

import id.ac.ui.cs.advprog.ordernotification.config.RabbitMQConfig;
import id.ac.ui.cs.advprog.ordernotification.event.WalletNotificationEvent;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WalletNotificationListener {

    private final NotificationService notificationService;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public WalletNotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.WALLET_QUEUE_NAME)
    public void handleWalletNotification(WalletNotificationEvent event) {
        log.info("Received RabbitMQ Message: Wallet {} for user {}", event.getType(), event.getUserId());
        notificationService.createWalletNotification(event);
    }
}
