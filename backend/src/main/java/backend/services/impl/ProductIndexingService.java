package backend.services.impl;

import backend.documents.BundleDocument;
import backend.documents.ProductDocument;
import backend.models.core.Product;
import backend.models.core.ProductBundle;
import backend.repositories.BundleRepository;
import backend.repositories.DiscountRepository;
import backend.repositories.ProductRepository;
import backend.repositories.search.BundleSearchRepository;
import backend.repositories.search.ProductSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class ProductIndexingService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProductIndexingService.class);

    private final ProductSearchRepository productSearchRepository;
    private final BundleSearchRepository bundleSearchRepository;
    private final ProductRepository productRepository;
    private final BundleRepository bundleRepository;
    private final DiscountRepository discountRepository;

    public ProductIndexingService(
            ProductSearchRepository productSearchRepository,
            BundleSearchRepository bundleSearchRepository,
            ProductRepository productRepository,
            BundleRepository bundleRepository,
            DiscountRepository discountRepository) {
        this.productSearchRepository = productSearchRepository;
        this.bundleSearchRepository = bundleSearchRepository;
        this.productRepository = productRepository;
        this.bundleRepository = bundleRepository;
        this.discountRepository = discountRepository;
    }

    @Async("searchExecutor")
    public void indexProduct(Product product) {
        try {
            productSearchRepository.save(toProductDocument(product));
        } catch (Exception e) {
            log.warn("[SEARCH INDEX] Failed to index product id={}: {}", product.getId(), e.getMessage());
        }
    }

    @Async("searchExecutor")
    public void removeProduct(long productId) {
        try {
            productSearchRepository.deleteById(productId);
        } catch (Exception e) {
            log.warn("[SEARCH INDEX] Failed to remove product id={} from index: {}", productId, e.getMessage());
        }
    }

    @Async("searchExecutor")
    public void indexBundle(ProductBundle bundle) {
        try {
            bundleSearchRepository.save(toBundleDocument(bundle));
        } catch (Exception e) {
            log.warn("[SEARCH INDEX] Failed to index bundle id={}: {}", bundle.getId(), e.getMessage());
        }
    }

    @Async("searchExecutor")
    public void removeBundle(long bundleId) {
        try {
            bundleSearchRepository.deleteById(bundleId);
        } catch (Exception e) {
            log.warn("[SEARCH INDEX] Failed to remove bundle id={} from index: {}", bundleId, e.getMessage());
        }
    }

    /**
     * On startup: bulk-index all existing products and bundles into Elasticsearch
     * if the index is empty. Skips silently if Elasticsearch is unavailable.
     */
    @Override
    public void run(ApplicationArguments args) {
        try {
            if (productSearchRepository.count() == 0) {
                for (Product p : productRepository.findAll()) {
                    try {
                        productSearchRepository.save(toProductDocument(p));
                    } catch (Exception ignored) {}
                }
                log.info("[SEARCH INDEX] Initial product index populated");
            }
        } catch (Exception e) {
            log.warn("[SEARCH INDEX] Product startup reindex skipped — Elasticsearch may be unavailable: {}", e.getMessage());
        }

        try {
            if (bundleSearchRepository.count() == 0) {
                for (ProductBundle b : bundleRepository.findAll()) {
                    try {
                        bundleSearchRepository.save(toBundleDocument(b));
                    } catch (Exception ignored) {}
                }
                log.info("[SEARCH INDEX] Initial bundle index populated");
            }
        } catch (Exception e) {
            log.warn("[SEARCH INDEX] Bundle startup reindex skipped — Elasticsearch may be unavailable: {}", e.getMessage());
        }
    }

    private ProductDocument toProductDocument(Product p) {
        List<String> discountCategories = discountRepository
                .findActiveDiscountCategoriesByProductId(p.getId(), Instant.now());

        return new ProductDocument(
                p.getId(),
                p.getCompany().getId(),
                p.getName(),
                p.getDescription(),
                p.getSku(),
                p.getCategory(),
                p.getBrand(),
                p.getTags(),
                p.getStatus() != null ? p.getStatus().name() : null,
                p.isFeatured(),
                p.isListed(),
                p.getPrice(),
                discountCategories.isEmpty() ? null : discountCategories
        );
    }

    private BundleDocument toBundleDocument(ProductBundle b) {
        return new BundleDocument(
                b.getId(),
                b.getCompany().getId(),
                b.getName(),
                b.getDescription(),
                b.getStatus() != null ? b.getStatus().name() : null,
                b.isListed(),
                b.getPrice()
        );
    }
}
