package id.ac.ui.cs.advprog.ordernotification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "feature")
public class FeatureFlagProperties {

    private boolean emailNotificationEnabled = true;
    private boolean pushNotificationEnabled = true;
    private boolean websocketLiveUpdates = true;
    private boolean simulationEndpointsEnabled = false;

    public boolean isEmailNotificationEnabled() {
        return emailNotificationEnabled;
    }

    public void setEmailNotificationEnabled(boolean emailNotificationEnabled) {
        this.emailNotificationEnabled = emailNotificationEnabled;
    }

    public boolean isPushNotificationEnabled() {
        return pushNotificationEnabled;
    }

    public void setPushNotificationEnabled(boolean pushNotificationEnabled) {
        this.pushNotificationEnabled = pushNotificationEnabled;
    }

    public boolean isWebsocketLiveUpdates() {
        return websocketLiveUpdates;
    }

    public void setWebsocketLiveUpdates(boolean websocketLiveUpdates) {
        this.websocketLiveUpdates = websocketLiveUpdates;
    }

    public boolean isSimulationEndpointsEnabled() {
        return simulationEndpointsEnabled;
    }

    public void setSimulationEndpointsEnabled(boolean simulationEndpointsEnabled) {
        this.simulationEndpointsEnabled = simulationEndpointsEnabled;
    }
}
