package id.ac.ui.cs.advprog.ordernotification.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FeatureFlagPropertiesTest {

    @Test
    void testDefaultValuesAllEnabled() {
        FeatureFlagProperties props = new FeatureFlagProperties();
        assertTrue(props.isEmailNotificationEnabled());
        assertTrue(props.isPushNotificationEnabled());
        assertTrue(props.isWebsocketLiveUpdates());
    }

    @Test
    void testSetEmailNotificationEnabled() {
        FeatureFlagProperties props = new FeatureFlagProperties();
        props.setEmailNotificationEnabled(false);
        assertFalse(props.isEmailNotificationEnabled());
    }

    @Test
    void testSetPushNotificationEnabled() {
        FeatureFlagProperties props = new FeatureFlagProperties();
        props.setPushNotificationEnabled(false);
        assertFalse(props.isPushNotificationEnabled());
    }

    @Test
    void testSetWebsocketLiveUpdates() {
        FeatureFlagProperties props = new FeatureFlagProperties();
        props.setWebsocketLiveUpdates(false);
        assertFalse(props.isWebsocketLiveUpdates());
    }

    @Test
    void testToggleFlagsOnAndOff() {
        FeatureFlagProperties props = new FeatureFlagProperties();

        props.setEmailNotificationEnabled(false);
        props.setPushNotificationEnabled(false);
        props.setWebsocketLiveUpdates(false);

        assertFalse(props.isEmailNotificationEnabled());
        assertFalse(props.isPushNotificationEnabled());
        assertFalse(props.isWebsocketLiveUpdates());

        props.setEmailNotificationEnabled(true);
        props.setPushNotificationEnabled(true);
        props.setWebsocketLiveUpdates(true);

        assertTrue(props.isEmailNotificationEnabled());
        assertTrue(props.isPushNotificationEnabled());
        assertTrue(props.isWebsocketLiveUpdates());
    }
}
