package backend.events.activity;

import java.time.Instant;

/**
 * Kafka payload for the product-events topic.
 * Consumed by the Python recsys service to keep embeddings and FAISS in sync.
 */
public record ProductChangedEvent(
    long productId,
    long marketplaceId,
    ChangeType changeType,
    Instant occurredAt
) {}
