package backend.services.impl;

import backend.events.activity.UserActivityEvent;
import backend.services.intf.ActivityEventPublisher;
import backend.services.intf.UserPreferenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class ActivityEventPublisherImpl implements ActivityEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ActivityEventPublisherImpl.class);

    private final KafkaTemplate<String, UserActivityEvent> kafkaTemplate;
    private final UserPreferenceService userPreferenceService;
    private final String topic;
    private final boolean trackingEnabled;

    public ActivityEventPublisherImpl(
            KafkaTemplate<String, UserActivityEvent> kafkaTemplate,
            UserPreferenceService userPreferenceService,
            @Value("${app.kafka.topics.user-activity}") String topic,
            @Value("${app.activity-tracking.enabled:true}") boolean trackingEnabled) {
        this.kafkaTemplate = kafkaTemplate;
        this.userPreferenceService = userPreferenceService;
        this.topic = topic;
        this.trackingEnabled = trackingEnabled;
    }

    @Override
    public void publish(UserActivityEvent event) {
        if (!trackingEnabled) return;
        if (event.marketplaceId() <= 0) {
            log.debug("Skipping activity event — no marketplaceId: productId={} type={}", event.productId(), event.activityType());
            return;
        }
        if (event.userId() != null && userPreferenceService.isTrackingOptedOut(event.userId())) {
            return;
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    doSend(event);
                }
            });
        } else {
            doSend(event);
        }
    }

    private void doSend(UserActivityEvent event) {
        try {
            String key = event.userId() != null ? String.valueOf(event.userId()) : event.sessionId();
            kafkaTemplate.send(topic, key, event).whenComplete((res, ex) -> {
                if (ex != null) {
                    log.warn("activity publish failed productId={} type={}", event.productId(), event.activityType(), ex);
                }
            });
        } catch (Throwable t) {
            log.warn("activity publish error productId={} type={}", event.productId(), event.activityType(), t);
        }
    }
}
