package backend.services.impl.pricing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import backend.models.core.Discount;
import backend.models.core.PromotionRule;
import backend.models.enums.DiscountType;
import backend.models.enums.PromotionRuleType;
import backend.repositories.DiscountRepository;
import backend.repositories.PromotionRuleRepository;

import java.util.HashSet;
import java.util.List;

/**
 * One-time startup migration that converts legacy {@link Discount} rows into {@link PromotionRule} rows
 * so the pricing engine can apply them. Idempotent: each new rule is stamped with
 * {@link PromotionRule#getLegacyDiscountId()} and re-runs skip already-migrated discounts.
 *
 * <p>Guarded by {@code app.pricing.migrate-discounts} (default {@code true}). Disable after Phase 5
 * removes the {@code Discount} entity.
 */
@Component
public class DiscountToPromotionRuleMigrator {

    private static final Logger log = LoggerFactory.getLogger(DiscountToPromotionRuleMigrator.class);

    private final DiscountRepository discountRepository;
    private final PromotionRuleRepository ruleRepository;
    private final ObjectMapper objectMapper;
    private final boolean enabled;

    public DiscountToPromotionRuleMigrator(
            DiscountRepository discountRepository,
            PromotionRuleRepository ruleRepository,
            ObjectMapper objectMapper,
            @Value("${app.pricing.migrate-discounts:true}") boolean enabled) {
        this.discountRepository = discountRepository;
        this.ruleRepository = ruleRepository;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void migrate() {
        if (!enabled) {
            log.debug("Discount -> PromotionRule migration skipped (app.pricing.migrate-discounts=false).");
            return;
        }

        List<Discount> all = discountRepository.findAll();
        if (all.isEmpty()) {
            log.debug("Discount -> PromotionRule migration: no discounts to migrate.");
            return;
        }

        int migrated = 0;
        int skipped = 0;
        for (Discount discount : all) {
            if (ruleRepository.findByLegacyDiscountId(discount.getId()).isPresent()) {
                skipped++;
                continue;
            }
            try {
                ruleRepository.save(buildRuleFromDiscount(discount));
                migrated++;
            } catch (JsonProcessingException e) {
                log.error("Failed to serialise configJson for Discount id={} — skipping", discount.getId(), e);
            }
        }
        log.info("Discount -> PromotionRule migration complete: migrated={}, alreadyPresent={}", migrated, skipped);
    }

    private PromotionRule buildRuleFromDiscount(Discount discount) throws JsonProcessingException {
        PromotionRule rule = new PromotionRule();
        rule.setCompany(discount.getCompany());
        rule.setName(discount.getName());
        rule.setDescription(discount.getDiscountCategory());
        rule.setRuleType(mapType(discount.getType()));
        rule.setStatus(discount.getStatus());
        rule.setPriority(100);
        rule.setStackable(false);
        rule.setStartDate(discount.getStartDate());
        rule.setEndDate(discount.getEndDate());
        rule.setConfigJson(buildConfigJson(discount));
        rule.setTargetProducts(new HashSet<>(discount.getProducts()));
        rule.setLegacyDiscountId(discount.getId());
        return rule;
    }

    private PromotionRuleType mapType(DiscountType type) {
        return switch (type) {
            case PERCENTAGE -> PromotionRuleType.PERCENTAGE_OFF;
            case FIXED_AMOUNT -> PromotionRuleType.FIXED_OFF;
        };
    }

    private String buildConfigJson(Discount discount) throws JsonProcessingException {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("appliesTo", "LINE");
        switch (discount.getType()) {
            case PERCENTAGE -> node.put("percent", discount.getValue());
            case FIXED_AMOUNT -> node.put("amount", discount.getValue());
        }
        return objectMapper.writeValueAsString(node);
    }
}
