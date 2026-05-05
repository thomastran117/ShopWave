package backend.events.activity;

import java.time.Instant;

public record BundleChangedEvent(
    long bundleId,
    ChangeType changeType,
    Instant occurredAt
) {}
