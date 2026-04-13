package backend.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import backend.repositories.DiscountRepository;

import java.time.Instant;

/**
 * Periodically hard-deletes discount rows whose {@code endDate} has passed.
 * The scheduler interval is controlled by {@code app.discount.expiry.interval-ms}
 * (default 1 hour). The real-time expiry check in {@code DiscountServiceImpl#toResponse}
 * means discounts appear as {@code EXPIRED} immediately — this job handles permanent cleanup.
 */
@Component
public class DiscountExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(DiscountExpiryScheduler.class);

    private final DiscountRepository discountRepository;

    public DiscountExpiryScheduler(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    @Scheduled(fixedDelayString = "${app.discount.expiry.interval-ms:3600000}")
    @Transactional
    public void deleteExpiredDiscounts() {
        int deleted = discountRepository.deleteAllExpiredBefore(Instant.now());
        if (deleted > 0) {
            log.info("[DISCOUNT EXPIRY] Deleted {} expired discount(s)", deleted);
        }
    }
}
