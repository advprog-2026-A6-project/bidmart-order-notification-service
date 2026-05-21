package id.ac.ui.cs.advprog.ordernotification.controller;

import id.ac.ui.cs.advprog.ordernotification.config.FeatureFlagProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/feature-flags")
public class FeatureFlagController {

    private final FeatureFlagProperties featureFlags;

    public FeatureFlagController(FeatureFlagProperties featureFlags) {
        this.featureFlags = featureFlags;
    }

    @GetMapping
    public Map<String, Boolean> getFeatureFlags() {
        Map<String, Boolean> flags = new LinkedHashMap<>();
        flags.put("emailNotificationEnabled", featureFlags.isEmailNotificationEnabled());
        flags.put("pushNotificationEnabled", featureFlags.isPushNotificationEnabled());
        flags.put("websocketLiveUpdates", featureFlags.isWebsocketLiveUpdates());
        return flags;
    }
}
