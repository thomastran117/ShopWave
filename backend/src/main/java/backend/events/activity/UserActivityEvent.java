package backend.events.activity;

import java.time.Instant;

/**
 * Kafka payload for the user-activity topic.
 * userId and sessionId are mutually exclusive: one is always null.
 * marketplaceId is guaranteed non-null before publish (publisher drops events where it is absent).
 */
public record UserActivityEvent(
    Long userId,
    String sessionId,
    long productId,
    long marketplaceId,
    ActivityType activityType,
    Instant occurredAt
) {}
