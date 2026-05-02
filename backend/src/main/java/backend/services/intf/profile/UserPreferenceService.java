package backend.services.intf.profile;

public interface UserPreferenceService {
    boolean isTrackingOptedOut(long userId);
    void setTrackingOptOut(long userId, boolean optOut);
}
