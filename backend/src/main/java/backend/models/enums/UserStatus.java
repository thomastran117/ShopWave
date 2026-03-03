package backend.models.enums;

/**
 * Lifecycle and access state of a user account.
 */
public enum UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    PENDING_VERIFICATION,
    DELETED
}
