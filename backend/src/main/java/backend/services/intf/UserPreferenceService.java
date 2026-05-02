package backend.services.intf;

public interface UserPreferenceService {
    boolean isTrackingOptedOut(long userId);
    void setTrackingOptOut(long userId, boolean optOut);
}
