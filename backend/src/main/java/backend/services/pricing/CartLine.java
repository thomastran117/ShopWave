package backend.services.pricing;

import java.math.BigDecimal;

/**
 * One line item in the pricing request. Prices are server-authoritative — callers pass
 * only (productId, variantId?, quantity); the engine resolves the current unit price
 * from the product/variant repository before constructing a CartLine.
 *
 * @param index          stable 0-based position in the original request, used for breakdowns
 * @param productId      owning product
 * @param variantId      optional variant; when present, overrides the product-level price
 * @param quantity       &ge; 1
 * @param unitBasePrice  pre-promotion unit price
 * @param companyId      product's owning company (used to scope rule candidates)
 */
public record CartLine(
        int index,
        long productId,
        Long variantId,
        int quantity,
        BigDecimal unitBasePrice,
        long companyId
) {}
