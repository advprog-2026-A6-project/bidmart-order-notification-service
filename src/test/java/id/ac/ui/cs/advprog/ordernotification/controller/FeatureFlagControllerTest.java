package id.ac.ui.cs.advprog.ordernotification.controller;

import id.ac.ui.cs.advprog.ordernotification.config.FeatureFlagProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FeatureFlagControllerTest {

    private FeatureFlagProperties featureFlags;
    private FeatureFlagController controller;

    @BeforeEach
    void setUp() {
        featureFlags = new FeatureFlagProperties();
        controller = new FeatureFlagController(featureFlags);
    }

    @Test
    void testGetFeatureFlagsAllEnabled() {
        Map<String, Boolean> flags = controller.getFeatureFlags();

        assertEquals(3, flags.size());
        assertTrue(flags.get("emailNotificationEnabled"));
        assertTrue(flags.get("pushNotificationEnabled"));
        assertTrue(flags.get("websocketLiveUpdates"));
    }

    @Test
    void testGetFeatureFlagsWithDisabledEmail() {
        featureFlags.setEmailNotificationEnabled(false);

        Map<String, Boolean> flags = controller.getFeatureFlags();

        assertFalse(flags.get("emailNotificationEnabled"));
        assertTrue(flags.get("pushNotificationEnabled"));
        assertTrue(flags.get("websocketLiveUpdates"));
    }

    @Test
    void testGetFeatureFlagsAllDisabled() {
        featureFlags.setEmailNotificationEnabled(false);
        featureFlags.setPushNotificationEnabled(false);
        featureFlags.setWebsocketLiveUpdates(false);

        Map<String, Boolean> flags = controller.getFeatureFlags();

        assertFalse(flags.get("emailNotificationEnabled"));
        assertFalse(flags.get("pushNotificationEnabled"));
        assertFalse(flags.get("websocketLiveUpdates"));
    }
}
