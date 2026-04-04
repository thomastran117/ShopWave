package backend.services.impl;

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
import backend.exceptions.http.BadRequestException;
import backend.exceptions.http.ForbiddenException;
import backend.exceptions.http.ResourceNotFoundException;
import backend.models.core.Company;
import backend.models.core.InventoryAdjustment;
import backend.models.core.Product;
import backend.repositories.CompanyRepository;
import backend.repositories.InventoryAdjustmentRepository;
import backend.repositories.ProductRepository;
import backend.repositories.UserRepository;
import backend.repositories.specifications.InventorySpecification;
import backend.services.intf.InventoryService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class InventoryServiceImpl implements InventoryService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("name", "price", "stock", "updatedAt");

    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final InventoryAdjustmentRepository adjustmentRepository;
    private final UserRepository userRepository;

    public InventoryServiceImpl(
            ProductRepository productRepository,
            CompanyRepository companyRepository,
            InventoryAdjustmentRepository adjustmentRepository,
            UserRepository userRepository) {
        this.productRepository = productRepository;
        this.companyRepository = companyRepository;
        this.adjustmentRepository = adjustmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public PagedResponse<InventoryItemResponse> getInventory(
            long companyId, long ownerId,
            String stockStatus, String q,
            int page, int size, String sort, String direction) {

        assertCompanyOwnership(companyId, ownerId);

        if (size > 50) size = 50;

        String sortField = (sort != null && SORTABLE_FIELDS.contains(sort)) ? sort : "updatedAt";
        Sort.Direction sortDir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortField));

        return new PagedResponse<>(
                productRepository
                        .findAll(InventorySpecification.withFilters(companyId, stockStatus, q), pageable)
                        .map(this::toInventoryItemResponse)
        );
    }

    @Override
    public InventorySummaryResponse getSummary(long companyId, long ownerId) {
        assertCompanyOwnership(companyId, ownerId);

        long totalProducts = productRepository.count(InventorySpecification.withFilters(companyId, null, null));
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

        int newStock = previousStock + delta;

        InventoryAdjustment adjustment = new InventoryAdjustment();
        adjustment.setProduct(productRepository.getReferenceById(productId));
        adjustment.setAdjustedBy(userRepository.getReferenceById(ownerId));
        adjustment.setDelta(delta);
        adjustment.setPreviousStock(previousStock);
        adjustment.setNewStock(newStock);
        adjustment.setReason(request.getReason());
        adjustment.setNote(request.getNote());
        adjustmentRepository.save(adjustment);

        product = productRepository.findByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        return toInventoryItemResponse(product);
    }

    @Override
    @Transactional
    public List<InventoryItemResponse> bulkAdjust(long companyId, long ownerId, BulkAdjustRequest request) {
        assertCompanyOwnership(companyId, ownerId);

        List<BulkAdjustItem> items = request.getItems();

        // Pass 1 — validation only, no writes
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

        // Pass 2 — apply all adjustments atomically (rollback on any failure)
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

    // --- Private helpers ---

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
