package backend.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import backend.models.core.Product;
import backend.models.core.ProductVariant;
import backend.repositories.ProductRepository;
import backend.repositories.ProductVariantRepository;
import backend.services.intf.EmailService;

/**
 * Evaluates low-stock and out-of-stock conditions and notifies the company owner
 * via terminal log and email. Supports both fixed-quantity and percent-of-max thresholds.
 *
 * Inject into any service that decrements stock.
 */
@Component
public class StockAlertService {

    private static final Logger log = LoggerFactory.getLogger(StockAlertService.class);

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final EmailService emailService;

    public StockAlertService(ProductRepository productRepository,
                             ProductVariantRepository variantRepository,
                             EmailService emailService) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.emailService = emailService;
    }

    /**
     * Checks product or variant stock after a decrement and alerts if any configured
     * threshold (fixed quantity OR percent of maxStock) is breached.
     *
     * @param productId   product ID (always present)
     * @param productName product name for readable output
     * @param variantId   null for product-level stock; variant ID for variant-level
     * @param variantSku  null for product-level; variant SKU for variant-level
     * @param newStock    the stock value AFTER the decrement
     * @param threshold   configured fixed low-stock threshold (null = no quantity threshold)
     */
    public void checkAndAlert(
            long productId,
            String productName,
            Long variantId,
            String variantSku,
            int newStock,
            Integer threshold) {

        // --- Resolve percent threshold from the entity (variant overrides product) ---
        Integer thresholdPercent = null;
        Integer maxStock = null;

        if (variantId != null) {
            ProductVariant variant = variantRepository.findById(variantId).orElse(null);
            if (variant != null) {
                thresholdPercent = variant.getLowStockThresholdPercent();
                maxStock = variant.getMaxStock();
            }
        } else {
            Product product = productRepository.findById(productId).orElse(null);
            if (product != null) {
                thresholdPercent = product.getLowStockThresholdPercent();
                maxStock = product.getMaxStock();
            }
        }

        boolean quantityBreached = threshold != null && newStock <= threshold;
        boolean percentBreached = thresholdPercent != null && maxStock != null
                && maxStock > 0 && (newStock * 100.0 / maxStock) <= thresholdPercent;

        if (!quantityBreached && !percentBreached) return;

        boolean outOfStock = newStock == 0;

        // --- Terminal log ---
        if (variantId != null) {
            if (outOfStock) {
                log.warn("[STOCK ALERT] OUT OF STOCK — product: '{}' (id={}) | variant SKU: '{}' (id={})",
                        productName, productId, variantSku, variantId);
            } else {
                log.warn("[STOCK ALERT] LOW STOCK — product: '{}' (id={}) | variant SKU: '{}' (id={}) | stock: {} (threshold: {}, thresholdPct: {}%)",
                        productName, productId, variantSku, variantId, newStock, threshold, thresholdPercent);
            }
        } else {
            if (outOfStock) {
                log.warn("[STOCK ALERT] OUT OF STOCK — product: '{}' (id={})",
                        productName, productId);
            } else {
                log.warn("[STOCK ALERT] LOW STOCK — product: '{}' (id={}) | stock: {} (threshold: {}, thresholdPct: {}%)",
                        productName, productId, newStock, threshold, thresholdPercent);
            }
        }

        // --- Email owner (one-shot fetch to resolve owner email) ---
        productRepository.findByIdWithCompanyOwner(productId).ifPresent(product -> {
            String ownerEmail = product.getCompany().getOwner().getEmail();
            String ownerFirstName = product.getCompany().getOwner().getFirstName();
            emailService.sendLowStockAlertEmail(
                    ownerEmail, ownerFirstName,
                    productId, productName,
                    variantId, variantSku,
                    newStock, threshold,
                    outOfStock);
        });
    }

    /**
     * Checks location-level stock after a decrement and logs a warning if at/below threshold.
     * Email notifications are not sent for location-level alerts.
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
