package id.ac.ui.cs.advprog.ordernotification.feature;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }


    @GetMapping
    @ResponseBody
    public List<Notification> getAll() {
        return service.getAll();
    }

    @PostMapping
    @ResponseBody
    public Notification create(
            @RequestParam String message,
            @RequestParam String type
    ) {
        return service.create(message, type);
    }

    @GetMapping("/view")
    public String viewPage() {
        return "notifications";
    }
}