package backend.repositories.specifications;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import backend.models.core.Company;
import backend.models.enums.CompanyStatus;

import java.util.ArrayList;
import java.util.List;

public class CompanySpecification {

    private CompanySpecification() {}

    /**
     * Builds a dynamic Specification from optional filter values.
     *
     * @param q        free-text search against name, description, and industry (case-insensitive LIKE)
     * @param industry exact match on industry
     * @param country  exact match on country
     * @param status   exact match on status enum; defaults to ACTIVE when null
     */
    public static Specification<Company> withFilters(
            String q,
            String industry,
            String country,
            CompanyStatus status) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (q != null && !q.isBlank()) {
                String pattern = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(root.get("industry")), pattern)
                ));
            }

            if (industry != null && !industry.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("industry")), industry.trim().toLowerCase()));
            }

            if (country != null && !country.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("country")), country.trim().toLowerCase()));
            }

            CompanyStatus effectiveStatus = status != null ? status : CompanyStatus.ACTIVE;
            predicates.add(cb.equal(root.get("status"), effectiveStatus));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
