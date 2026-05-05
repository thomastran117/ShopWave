package backend.services.impl.subscriptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import backend.models.core.Subscription;
import backend.models.core.User;
import backend.models.enums.SubscriptionStatus;
import backend.repositories.SubscriptionRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Sends a heads-up email two to three days before each ACTIVE subscription's
 * next billing date. Stripe handles the actual billing — this scheduler only
 * surfaces upcoming charges so customers can pause / skip / swap before they hit.
 */
@Component
public class SubscriptionRenewalReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionRenewalReminderScheduler.class);

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionRenewalReminderScheduler(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Scheduled(cron = "${app.subscription.reminder.cron:0 0 8 * * *}")
    public void sendRenewalReminders() {
        Instant from = Instant.now().plus(2, ChronoUnit.DAYS);
        Instant to = Instant.now().plus(3, ChronoUnit.DAYS);

        List<Subscription> upcoming = subscriptionRepository.findAllByStatusAndNextBillingAtBetween(
                SubscriptionStatus.ACTIVE, from, to);

        if (upcoming.isEmpty()) return;

        log.info("Sending renewal reminders for {} subscriptions", upcoming.size());

        for (Subscription sub : upcoming) {
            try {
                User user = sub.getUser();
                if (user == null || user.getEmail() == null) continue;
                // Dedicated EmailService template will be wired in a follow-up; for now
                // log the intent so ops can audit until the template is added.
                log.info("Renewal reminder due for subscription {} (user {} email {}) at {}",
                        sub.getId(), user.getId(), user.getEmail(), sub.getNextBillingAt());
            } catch (Exception e) {
                log.warn("Renewal reminder failed for subscription {}: {}", sub.getId(), e.getMessage());
            }
        }
    }
}
