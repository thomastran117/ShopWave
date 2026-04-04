package backend.repositories.specifications;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import backend.models.core.Product;

import java.util.ArrayList;
import java.util.List;

public class InventorySpecification {

    private InventorySpecification() {}

    /**
     * Builds a dynamic specification for inventory queries.
     *
     * @param stockStatus ALL | IN_STOCK | LOW_STOCK | OUT_OF_STOCK | UNTRACKED
     * @param q           free-text search on name and sku (case-insensitive)
     */
    public static Specification<Product> withFilters(long companyId, String stockStatus, String q) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("company").get("id"), companyId));

            if (q != null && !q.isBlank()) {
                String pattern = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("sku")), pattern)
                ));
            }

            if (stockStatus != null) {
                switch (stockStatus.toUpperCase()) {
                    case "OUT_OF_STOCK" -> predicates.add(cb.and(
                            cb.isNotNull(root.get("stock")),
                            cb.equal(root.get("stock"), 0)));
                    case "IN_STOCK" -> predicates.add(cb.and(
                            cb.isNotNull(root.get("stock")),
                            cb.greaterThan(root.get("stock"), 0)));
                    case "LOW_STOCK" -> predicates.add(cb.and(
                            cb.isNotNull(root.get("stock")),
                            cb.isNotNull(root.get("lowStockThreshold")),
                            cb.greaterThan(root.get("stock"), 0),
                            cb.lessThanOrEqualTo(root.get("stock"), root.get("lowStockThreshold"))));
                    case "UNTRACKED" -> predicates.add(cb.isNull(root.get("stock")));
                    // "ALL" and unknown values: no additional predicate
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
