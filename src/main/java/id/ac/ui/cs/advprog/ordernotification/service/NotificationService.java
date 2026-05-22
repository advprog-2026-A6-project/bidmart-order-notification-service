package id.ac.ui.cs.advprog.ordernotification.service;

import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import id.ac.ui.cs.advprog.ordernotification.model.NotificationPreference;
import id.ac.ui.cs.advprog.ordernotification.model.Order;
import id.ac.ui.cs.advprog.ordernotification.model.AuctionEventMessage;
import id.ac.ui.cs.advprog.ordernotification.model.AuctionFinishedMessage;
import id.ac.ui.cs.advprog.ordernotification.event.WalletNotificationEvent;

import java.util.List;

public interface NotificationService {
    Notification create(Notification notification);
    List<Notification> findAll();
    List<Notification> findByUserId(String userId);
    void createOrderNotification(Order order);
    NotificationPreference setPreference(String userId, String email, boolean emailEnabled, boolean pushEnabled);
    NotificationPreference getPreference(String userId);
    void sendNotification(String userId, String message, String type);
    void sendNotification(String userId, String pushMessage, String emailMessage, String type);
    void createWalletNotification(WalletNotificationEvent event);
    void createAuctionBidNotification(AuctionEventMessage event);
    void createAuctionOutbidNotification(AuctionEventMessage event);
    void createAuctionWonNotification(AuctionFinishedMessage message);
}
