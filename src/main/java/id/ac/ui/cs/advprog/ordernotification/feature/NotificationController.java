package id.ac.ui.cs.advprog.ordernotification.feature;

import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.util.List;

@Controller
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    // API (JSON)
    @GetMapping("/api/notifications")
    @ResponseBody
    public List<Notification> getNotifications() {
        return service.getAll();
    }

    // Web page
    @GetMapping("/notifications")
    public String notificationsPage(Model model) {
        return "notifications";
    }
}
