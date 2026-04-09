package backend.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Logs low-stock and out-of-stock alerts to the terminal.
 * Inject into any service that decrements stock.
 * Future: add email/webhook notification here.
 */
@Component
public class StockAlertService {

    private static final Logger log = LoggerFactory.getLogger(StockAlertService.class);

    /**
     * Checks product or variant stock after a decrement and logs a warning if at/below threshold.
     *
     * @param productId   product ID (always present)
     * @param productName product name for readable output
     * @param variantId   null for product-level stock; variant ID for variant-level
     * @param variantSku  null for product-level; variant SKU for variant-level
     * @param newStock    the stock value AFTER the decrement
     * @param threshold   configured low-stock threshold (null = no alert configured)
     */
    public void checkAndAlert(
            long productId,
            String productName,
            Long variantId,
            String variantSku,
            int newStock,
            Integer threshold) {

        if (threshold == null || newStock > threshold) return;

        if (variantId != null) {
            if (newStock == 0) {
                log.warn("[STOCK ALERT] OUT OF STOCK — product: '{}' (id={}) | variant SKU: '{}' (id={})",
                        productName, productId, variantSku, variantId);
            } else {
                log.warn("[STOCK ALERT] LOW STOCK — product: '{}' (id={}) | variant SKU: '{}' (id={}) | stock: {} (threshold: {})",
                        productName, productId, variantSku, variantId, newStock, threshold);
            }
        } else {
            if (newStock == 0) {
                log.warn("[STOCK ALERT] OUT OF STOCK — product: '{}' (id={})",
                        productName, productId);
            } else {
                log.warn("[STOCK ALERT] LOW STOCK — product: '{}' (id={}) | stock: {} (threshold: {})",
                        productName, productId, newStock, threshold);
            }
        }
    }

    /**
     * Checks location-level stock after a decrement and logs a warning if at/below threshold.
     */
    public void checkAndAlertLocation(
            long locationStockId,
            String locationName,
            long productId,
            String productName,
            Long variantId,
            int newStock,
            Integer threshold) {

        if (threshold == null || newStock > threshold) return;

        if (newStock == 0) {
            log.warn("[STOCK ALERT] LOCATION OUT OF STOCK — location: '{}' | product: '{}' (id={}) | variantId: {} | locationStockId: {}",
                    locationName, productName, productId, variantId, locationStockId);
        } else {
            log.warn("[STOCK ALERT] LOCATION LOW STOCK — location: '{}' | product: '{}' (id={}) | variantId: {} | locationStockId: {} | stock: {} (threshold: {})",
                    locationName, productName, productId, variantId, locationStockId, newStock, threshold);
        }
    }
}
