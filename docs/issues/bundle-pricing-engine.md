# Feature: Bundle-Level Pricing Engine Integration

## Summary

`ProductBundle` entities currently have no direct relationship with `PromotionRule`. As a result, the pricing engine cannot scope a promotion rule specifically to a bundle — rules can only target individual `Product` entities via `PromotionRule.targetProducts`.

This issue tracks the work needed to allow a promotion rule to fire specifically when a bundle is in the cart, rather than when any of its component products appear individually.

---

## Current Behaviour

`PromotionRule` has:

```java
@ManyToMany
Set<Product> targetProducts;   // empty = entire company catalogue
```

`ProductBundle` has no reference back to `PromotionRule`. The pricing engine evaluates cart lines, each of which maps to a `Product` (or `ProductVariant`). When a bundle is added to the cart it is expanded into individual product lines — at which point the engine loses the information that those lines originated from a bundle purchase.

**Side-effect:** a PERCENTAGE_OFF rule targeting the components of a "Work From Home Pro" bundle would fire even when a customer buys the same products individually, not as a bundle.

---

## Desired Behaviour

A `PromotionRule` should be optionally scopeable to one or more `ProductBundle` entities. The engine should:

1. Detect that a set of cart lines originated from a bundle purchase.
2. Evaluate bundle-scoped rules only when the bundle is the source.
3. Skip bundle-scoped rules when the same products are purchased individually.

---

## Proposed Changes

### 1. Data Model — `PromotionRule`

Add an optional many-to-many relationship to `ProductBundle`:

```java
// PromotionRule.java
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(
    name = "promotion_rule_bundles",
    joinColumns = @JoinColumn(name = "rule_id"),
    inverseJoinColumns = @JoinColumn(name = "bundle_id")
)
@BatchSize(size = 50)
private Set<ProductBundle> targetBundles = new HashSet<>();
```

Semantics:
- `targetBundles` empty — rule is **not** bundle-scoped (current behaviour, unchanged).
- `targetBundles` non-empty — rule fires **only** when the cart contains a matching bundle.
- A rule can have both `targetProducts` and `targetBundles`; the engine evaluates the union.

### 2. Database Migration

New join table:

```sql
CREATE TABLE promotion_rule_bundles (
    rule_id   BIGINT NOT NULL,
    bundle_id BIGINT NOT NULL,
    PRIMARY KEY (rule_id, bundle_id),
    CONSTRAINT fk_prb_rule   FOREIGN KEY (rule_id)   REFERENCES promotion_rules(id)   ON DELETE CASCADE,
    CONSTRAINT fk_prb_bundle FOREIGN KEY (bundle_id) REFERENCES product_bundles(id) ON DELETE CASCADE
);
CREATE INDEX idx_prb_rule   ON promotion_rule_bundles (rule_id);
CREATE INDEX idx_prb_bundle ON promotion_rule_bundles (bundle_id);
```

### 3. Cart / Order Model

The cart line (or `OrderItem`) must carry a nullable `bundleId` to preserve the bundle origin through checkout:

```java
// OrderItem.java
@Column(name = "bundle_id", nullable = true)
private Long bundleId;
```

Populated by the checkout service when a cart line is created from a bundle add-to-cart action.

### 4. Pricing Engine — `PricingEngineImpl`

Update `linesForRule()` to handle bundle-scoped rules:

```java
private List<CartLine> linesForRule(PromotionRule rule, List<CartLine> companyLines) {
    Set<Long> productIds = rule.getTargetProducts().stream()
            .map(Product::getId).collect(toSet());
    Set<Long> bundleIds = rule.getTargetBundles().stream()
            .map(ProductBundle::getId).collect(toSet());

    if (!bundleIds.isEmpty()) {
        // Bundle-scoped: only lines that originated from a target bundle
        return companyLines.stream()
                .filter(line -> line.getBundleId() != null
                             && bundleIds.contains(line.getBundleId()))
                .filter(line -> productIds.isEmpty()
                             || productIds.contains(line.getProductId()))
                .toList();
    }

    // Existing logic unchanged
    if (productIds.isEmpty()) return companyLines;
    return companyLines.stream()
            .filter(line -> productIds.contains(line.getProductId()))
            .toList();
}
```

### 5. `PromotionRuleRepository`

Add a query that includes bundle-scoped candidates when a bundle is in the cart:

```java
@Query("""
    SELECT DISTINCT r FROM PromotionRule r
    LEFT JOIN r.targetBundles tb
    WHERE r.company.id IN :companyIds
      AND r.status = 'ACTIVE'
      AND (r.startDate IS NULL OR r.startDate <= :now)
      AND (r.endDate   IS NULL OR r.endDate   >= :now)
      AND (tb.id IN :bundleIds OR NOT EXISTS (
              SELECT 1 FROM PromotionRule r2
              JOIN r2.targetBundles tb2 WHERE r2 = r))
    """)
List<PromotionRule> findActiveCandidatesWithBundles(
        @Param("companyIds") Collection<Long> companyIds,
        @Param("bundleIds")  Collection<Long> bundleIds,
        @Param("now")        Instant now);
```

### 6. Admin API / DTO

Expose `targetBundleIds: List<Long>` alongside the existing `targetProductIds` in `PromotionRuleRequest` and `PromotionRuleResponse`.

### 7. Dev Seeder Update

Once this feature ships, update `BundleSeeder` to link each company's featured bundle to a dedicated `PromotionRule`. The `PricingEngineSeeder` can remove the product-level approximation rules that were added as a workaround.

---

## Affected Files

| File | Change |
|------|--------|
| `backend/models/core/PromotionRule.java` | Add `targetBundles` ManyToMany |
| `backend/models/core/OrderItem.java` | Add nullable `bundleId` column |
| `backend/repositories/PromotionRuleRepository.java` | New query including bundle candidates |
| `backend/services/impl/pricing/PricingEngineImpl.java` | Update `linesForRule()` |
| `backend/dtos/` | Expose `targetBundleIds` in rule request/response DTOs |
| `backend/seeds/BundleSeeder.java` | Link featured bundles to pricing rules |
| Migration | `CREATE TABLE promotion_rule_bundles` + `ALTER TABLE order_items ADD bundle_id` |

---

## Acceptance Criteria

- [ ] A `PromotionRule` with `targetBundles = [bundleId]` fires when the cart contains that bundle.
- [ ] The same rule does **not** fire when the bundle's component products are purchased individually.
- [ ] A rule with both `targetBundles` and `targetProducts` populated respects both constraints.
- [ ] Existing rules with empty `targetBundles` behave identically to today.
- [ ] Dev seeders demonstrate at least one bundle-scoped rule per company.
- [ ] Admin API returns and accepts `targetBundleIds` on promotion rule endpoints.
