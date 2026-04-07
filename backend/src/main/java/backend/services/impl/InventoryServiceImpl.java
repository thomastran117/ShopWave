package backend.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.dtos.requests.inventory.AdjustStockRequest;
import backend.dtos.requests.inventory.BulkAdjustItem;
import backend.dtos.requests.inventory.BulkAdjustRequest;
import backend.dtos.requests.inventory.UpdateInventorySettingsRequest;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.inventory.AdjustmentResponse;
import backend.dtos.responses.inventory.InventoryItemResponse;
import backend.dtos.responses.inventory.InventorySummaryResponse;
import backend.dtos.responses.inventory.ProductSalesMetricResponse;
import backend.repositories.projections.ProductSalesProjection;
import backend.exceptions.http.BadRequestException;
import backend.exceptions.http.ConflictException;
import backend.exceptions.http.ForbiddenException;
import backend.exceptions.http.ResourceNotFoundException;
import backend.models.core.Company;
import backend.models.enums.ProductStatus;
import backend.models.core.InventoryAdjustment;
import backend.models.core.Product;
import backend.repositories.CompanyRepository;
import backend.repositories.InventoryAdjustmentRepository;
import backend.repositories.ProductRepository;
import backend.repositories.UserRepository;
import backend.repositories.specifications.InventorySpecification;
import backend.services.intf.CacheService;
import backend.services.intf.InventoryService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);

    // Must match the lock key used by OrderServiceImpl to ensure mutual exclusion
    private static final String LOCK_PREFIX = "lock:product:";
    private static final long LOCK_TTL_SECONDS = 10;
    private static final int LOCK_RETRY_ATTEMPTS = 5;
    private static final long LOCK_RETRY_DELAY_MS = 100;

    private static final Set<String> SORTABLE_FIELDS = Set.of("name", "price", "stock", "updatedAt");

    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final InventoryAdjustmentRepository adjustmentRepository;
    private final UserRepository userRepository;
    private final CacheService cacheService;

    public InventoryServiceImpl(
            ProductRepository productRepository,
            CompanyRepository companyRepository,
            InventoryAdjustmentRepository adjustmentRepository,
            UserRepository userRepository,
            CacheService cacheService) {
        this.productRepository = productRepository;
        this.companyRepository = companyRepository;
        this.adjustmentRepository = adjustmentRepository;
        this.userRepository = userRepository;
        this.cacheService = cacheService;
    }

    @Override
    public PagedResponse<InventoryItemResponse> getInventory(
            long companyId, long ownerId,
            String stockStatus, String q,
            String category, String brand,
            ProductStatus status, Integer minStock, Integer maxStock,
            int page, int size, String sort, String direction) {

        assertCompanyOwnership(companyId, ownerId);

        if (size > 50) size = 50;

        String sortField = (sort != null && SORTABLE_FIELDS.contains(sort)) ? sort : "updatedAt";
        Sort.Direction sortDir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortField));

        return new PagedResponse<>(
                productRepository
                        .findAll(InventorySpecification.withFilters(
                                companyId, stockStatus, q, category, brand, status, minStock, maxStock), pageable)
                        .map(this::toInventoryItemResponse)
        );
    }

    @Override
    public InventorySummaryResponse getSummary(long companyId, long ownerId) {
        assertCompanyOwnership(companyId, ownerId);

        long totalProducts = productRepository.count(
                InventorySpecification.withFilters(companyId, null, null, null, null, null, null, null));
        long trackedProducts = productRepository.countTrackedProducts(companyId);
        long outOfStockCount = productRepository.countOutOfStock(companyId);
        long lowStockCount = productRepository.countLowStock(companyId);
        long inStockCount = trackedProducts - outOfStockCount - lowStockCount;
        BigDecimal totalInventoryValue = productRepository.totalInventoryValue(companyId);

        return new InventorySummaryResponse(
                totalProducts,
                trackedProducts,
                inStockCount,
                lowStockCount,
                outOfStockCount,
                totalInventoryValue
        );
    }

    @Override
    public InventoryItemResponse getInventoryItem(long companyId, long productId, long ownerId) {
        assertCompanyOwnership(companyId, ownerId);

        Product product = productRepository.findByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        return toInventoryItemResponse(product);
    }

    @Override
    public PagedResponse<AdjustmentResponse> getAdjustmentHistory(
            long companyId, long productId, long ownerId, int page, int size) {

        assertCompanyOwnership(companyId, ownerId);

        productRepository.findByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return new PagedResponse<>(
                adjustmentRepository
                        .findAllByProductIdAndProductCompanyId(productId, companyId, pageable)
                        .map(this::toAdjustmentResponse)
        );
    }

    @Override
    @Transactional
    public InventoryItemResponse adjustStock(
            long companyId, long productId, long ownerId, AdjustStockRequest request) {

        assertCompanyOwnership(companyId, ownerId);

        String lockToken = UUID.randomUUID().toString();
        String lockKey = LOCK_PREFIX + productId;
        boolean lockAcquired = false;

        try {
            for (int attempt = 0; attempt < LOCK_RETRY_ATTEMPTS; attempt++) {
                if (cacheService.tryLock(lockKey, lockToken, LOCK_TTL_SECONDS)) {
                    lockAcquired = true;
                    break;
                }
                try {
                    Thread.sleep(LOCK_RETRY_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new ConflictException("Stock adjustment interrupted, please try again");
                }
            }

            if (!lockAcquired) {
                throw new ConflictException("Product is currently being updated by another operation, please try again shortly");
            }

            Product product = productRepository.findByIdAndCompanyId(productId, companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

            if (product.getStock() == null) {
                throw new BadRequestException("Stock tracking is not enabled for this product. Set an initial stock value first.");
            }

            int previousStock = product.getStock();
            int delta = request.getDelta();

            int rows = productRepository.adjustStock(productId, delta);
            if (rows == 0) {
                throw new BadRequestException(
                        "Adjustment would result in negative stock. Current stock: " + previousStock + ", delta: " + delta);
            }

            InventoryAdjustment adjustment = new InventoryAdjustment();
            adjustment.setProduct(productRepository.getReferenceById(productId));
            adjustment.setAdjustedBy(userRepository.getReferenceById(ownerId));
            adjustment.setDelta(delta);
            adjustment.setPreviousStock(previousStock);
            adjustment.setNewStock(previousStock + delta);
            adjustment.setReason(request.getReason());
            adjustment.setNote(request.getNote());
            adjustmentRepository.save(adjustment);

            product = productRepository.findByIdAndCompanyId(productId, companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

            return toInventoryItemResponse(product);

        } finally {
            if (lockAcquired) {
                try {
                    cacheService.unlock(lockKey, lockToken);
                } catch (Exception e) {
                    log.error("Failed to release inventory lock for product {}: {}", productId, e.getMessage());
                }
            }
        }
    }

    @Override
    @Transactional
    public List<InventoryItemResponse> bulkAdjust(long companyId, long ownerId, BulkAdjustRequest request) {
        assertCompanyOwnership(companyId, ownerId);

        List<BulkAdjustItem> items = request.getItems();

        // Deduplicate and sort product IDs ascending — consistent lock ordering prevents deadlocks
        // when concurrent bulk operations overlap on the same products.
        List<Long> sortedProductIds = items.stream()
                .map(BulkAdjustItem::getProductId)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        String lockToken = UUID.randomUUID().toString();
        List<String> acquiredLocks = new ArrayList<>();

        try {
            acquireLocks(sortedProductIds, lockToken, acquiredLocks);

            // Pass 1 — validate all items before any writes
            List<String> errors = new ArrayList<>();
            for (BulkAdjustItem item : items) {
                productRepository.findByIdAndCompanyId(item.getProductId(), companyId)
                        .ifPresentOrElse(
                                product -> {
                                    if (product.getStock() == null) {
                                        errors.add("Product " + item.getProductId() + " does not have stock tracking enabled");
                                    }
                                },
                                () -> errors.add("Product " + item.getProductId() + " not found in this company")
                        );
            }

            if (!errors.isEmpty()) {
                throw new BadRequestException("Bulk adjustment validation failed: " + String.join("; ", errors));
            }

            // Pass 2 — apply all adjustments; any failure rolls back the entire transaction
            List<InventoryAdjustment> adjustments = new ArrayList<>();
            for (BulkAdjustItem item : items) {
                Product product = productRepository.findByIdAndCompanyId(item.getProductId(), companyId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + item.getProductId()));

                int previousStock = product.getStock();
                int delta = item.getDelta();

                int rows = productRepository.adjustStock(item.getProductId(), delta);
                if (rows == 0) {
                    throw new BadRequestException(
                            "Adjustment for product " + item.getProductId() +
                            " would result in negative stock. Current stock: " + previousStock + ", delta: " + delta);
                }

                InventoryAdjustment adjustment = new InventoryAdjustment();
                adjustment.setProduct(productRepository.getReferenceById(item.getProductId()));
                adjustment.setAdjustedBy(userRepository.getReferenceById(ownerId));
                adjustment.setDelta(delta);
                adjustment.setPreviousStock(previousStock);
                adjustment.setNewStock(previousStock + delta);
                adjustment.setReason(item.getReason());
                adjustment.setNote(item.getNote());
                adjustments.add(adjustment);
            }

            adjustmentRepository.saveAll(adjustments);

            return items.stream()
                    .map(item -> productRepository.findByIdAndCompanyId(item.getProductId(), companyId)
                            .map(this::toInventoryItemResponse)
                            .orElseThrow())
                    .toList();

        } finally {
            releaseLocks(acquiredLocks, lockToken);
        }
    }

    @Override
    @Transactional
    public InventoryItemResponse updateSettings(
            long companyId, long productId, long ownerId, UpdateInventorySettingsRequest request) {

        assertCompanyOwnership(companyId, ownerId);

        Product product = productRepository.findByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.setLowStockThreshold(request.getLowStockThreshold());
        productRepository.save(product);

        return toInventoryItemResponse(product);
    }

    @Override
    public List<ProductSalesMetricResponse> getTopPurchasedProducts(long companyId, long ownerId, int limit, Instant from, Instant to) {
        assertCompanyOwnership(companyId, ownerId);
        return productRepository.findTopByUnitsSold(companyId, limit, from, to)
                .stream()
                .map(this::toSalesMetricResponse)
                .toList();
    }

    @Override
    public List<ProductSalesMetricResponse> getTopRevenueProducts(long companyId, long ownerId, int limit, Instant from, Instant to) {
        assertCompanyOwnership(companyId, ownerId);
        return productRepository.findTopByRevenue(companyId, limit, from, to)
                .stream()
                .map(this::toSalesMetricResponse)
                .toList();
    }

    @Override
    public List<ProductSalesMetricResponse> getNeverSoldProducts(long companyId, long ownerId, int limit) {
        assertCompanyOwnership(companyId, ownerId);
        return productRepository.findNeverSold(companyId, limit)
                .stream()
                .map(this::toSalesMetricResponse)
                .toList();
    }

    // --- Lock helpers (mirrors OrderServiceImpl — same lock namespace) ---

    private void acquireLocks(List<Long> sortedProductIds, String lockToken, List<String> acquiredLocks) {
        for (Long productId : sortedProductIds) {
            String lockKey = LOCK_PREFIX + productId;
            boolean acquired = false;

            for (int attempt = 0; attempt < LOCK_RETRY_ATTEMPTS; attempt++) {
                if (cacheService.tryLock(lockKey, lockToken, LOCK_TTL_SECONDS)) {
                    acquiredLocks.add(lockKey);
                    acquired = true;
                    break;
                }
                try {
                    Thread.sleep(LOCK_RETRY_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    releaseLocks(acquiredLocks, lockToken);
                    throw new ConflictException("Bulk adjustment interrupted, please try again");
                }
            }

            if (!acquired) {
                releaseLocks(acquiredLocks, lockToken);
                throw new ConflictException(
                        "Product " + productId + " is currently being updated by another operation, please try again shortly");
            }
        }
    }

    private void releaseLocks(List<String> lockKeys, String lockToken) {
        for (String lockKey : lockKeys) {
            try {
                cacheService.unlock(lockKey, lockToken);
            } catch (Exception e) {
                log.error("Failed to release inventory lock {}: {}", lockKey, e.getMessage());
            }
        }
    }

    // --- Private mapping helpers ---

    private Company assertCompanyOwnership(long companyId, long ownerId) {
        return companyRepository.findByIdAndOwnerId(companyId, ownerId)
                .orElseThrow(() -> new ForbiddenException("You do not own this company"));
    }

    private InventoryItemResponse toInventoryItemResponse(Product product) {
        Integer stock = product.getStock();
        Integer threshold = product.getLowStockThreshold();

        boolean untracked = stock == null;
        boolean outOfStock = !untracked && stock.equals(0);
        boolean lowStock = !untracked && !outOfStock && threshold != null && stock.compareTo(threshold) <= 0;

        String stockStatus;
        if (untracked)       stockStatus = "UNTRACKED";
        else if (outOfStock) stockStatus = "OUT_OF_STOCK";
        else if (lowStock)   stockStatus = "LOW_STOCK";
        else                 stockStatus = "IN_STOCK";

        return new InventoryItemResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                stock,
                threshold,
                lowStock,
                outOfStock,
                stockStatus,
                product.getPrice(),
                product.getCurrency(),
                product.getUpdatedAt()
        );
    }

    private ProductSalesMetricResponse toSalesMetricResponse(ProductSalesProjection p) {
        BigDecimal stockValue = null;
        if (p.getCurrentStock() != null && p.getPrice() != null) {
            stockValue = p.getPrice().multiply(BigDecimal.valueOf(p.getCurrentStock()));
        }
        return new ProductSalesMetricResponse(
                p.getProductId(),
                p.getProductName(),
                p.getSku(),
                p.getCurrentStock(),
                p.getPrice(),
                p.getCurrency(),
                p.getTotalUnitsSold() != null ? p.getTotalUnitsSold() : 0L,
                p.getTotalRevenue() != null ? p.getTotalRevenue() : BigDecimal.ZERO,
                stockValue
        );
    }

    private AdjustmentResponse toAdjustmentResponse(InventoryAdjustment adj) {
        return new AdjustmentResponse(
                adj.getId(),
                adj.getProduct().getId(),
                adj.getProduct().getName(),
                adj.getAdjustedBy() != null ? adj.getAdjustedBy().getId() : null,
                adj.getDelta(),
                adj.getPreviousStock(),
                adj.getNewStock(),
                adj.getReason().name(),
                adj.getNote(),
                adj.getCreatedAt()
        );
    }
}
