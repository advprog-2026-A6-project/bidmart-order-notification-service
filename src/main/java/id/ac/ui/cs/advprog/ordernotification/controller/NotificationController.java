package id.ac.ui.cs.advprog.ordernotification.controller;

import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import id.ac.ui.cs.advprog.ordernotification.service.NotificationService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;
    
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public List<Notification> getNotifications() {
        return service.findAll();
    }

    @PostMapping
    public Notification createNotification(
            @RequestParam String message,
            @RequestParam String type) {

        Notification notif = new Notification();
        notif.setMessage(message);
        notif.setType(type);

        return service.create(notif);
    }
}