package id.ac.ui.cs.advprog.ordernotification.controller;

import id.ac.ui.cs.advprog.ordernotification.config.FeatureFlagProperties;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Controller
public class PageController {

    private final FeatureFlagProperties featureFlags;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public PageController(FeatureFlagProperties featureFlags) {
        this.featureFlags = featureFlags;
    }

    @GetMapping("/")
    public String home() {
        return "orderList";
    }

    @GetMapping("/orders")
    public String ordersPage() {
        return "orderList";
    }

    @GetMapping("/create-order")
    public String createOrderPage() {
        return "createOrder";
    }

    @GetMapping("/notifications")
    public String notificationsPage() {
        return "notificationList";
    }

    @GetMapping("/preferences")
    public String preferencesPage() {
        return "preferences";
    }

    @GetMapping("/simulate")
    public String simulatePage() {
        if (!featureFlags.isSimulationEndpointsEnabled()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return "auctionFinish";
    }
}
