package backend.services.impl.products;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.dtos.requests.product.BundleItemRequest;
import backend.dtos.requests.product.CreateBundleRequest;
import backend.dtos.requests.product.UpdateBundleRequest;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.product.BundleItemResponse;
import backend.dtos.responses.product.BundleResponse;
import backend.exceptions.http.BadRequestException;
import backend.exceptions.http.ForbiddenException;
import backend.exceptions.http.ResourceNotFoundException;
import backend.models.core.BundleItem;
import backend.models.core.Product;
import backend.models.core.ProductBundle;
import backend.models.core.ProductVariant;
import backend.models.enums.ProductStatus;
import backend.repositories.BundleRepository;
import backend.repositories.CompanyRepository;
import backend.repositories.ProductRepository;
import backend.repositories.ProductVariantRepository;
import backend.events.BundleIndexEvent;
import backend.events.BundleRemoveEvent;
import backend.services.intf.products.BundleService;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BundleServiceImpl implements BundleService {

    private final BundleRepository bundleRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final CompanyRepository companyRepository;
    private final ApplicationEventPublisher eventPublisher;

    public BundleServiceImpl(
            BundleRepository bundleRepository,
            ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            CompanyRepository companyRepository,
            ApplicationEventPublisher eventPublisher) {
        this.bundleRepository = bundleRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.companyRepository = companyRepository;
        this.eventPublisher = eventPublisher;
    }

    // --- Owner-authenticated CRUD ---

    @Override
    public PagedResponse<BundleResponse> listBundles(long companyId, long ownerId, ProductStatus status, int page, int size) {
        assertOwnership(companyId, ownerId);
        return listBundles(companyId, status, page, size);
    }

    @Override
    @Transactional
    public BundleResponse createBundle(long companyId, long ownerId, CreateBundleRequest request) {
        var company = companyRepository.findByIdAndOwnerId(companyId, ownerId)
                .orElseThrow(() -> new ForbiddenException("You do not own this company"));

        List<BundleItemRequest> itemRequests = request.getItems();
        validateNoDuplicates(itemRequests);

        ProductBundle bundle = new ProductBundle();
        bundle.setCompany(company);
        bundle.setName(request.getName());
        bundle.setDescription(request.getDescription());
        bundle.setThumbnailUrl(request.getThumbnailUrl());
        bundle.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        bundle.setListed(request.isListed());

        List<BundleItem> items = buildItems(bundle, itemRequests, companyId);
        bundle.setItems(items);

        BigDecimal price = request.getPrice() != null
                ? request.getPrice()
                : computePrice(items);
        bundle.setPrice(price);

        if (request.getCompareAtPrice() != null) bundle.setCompareAtPrice(request.getCompareAtPrice());

        ProductBundle saved = bundleRepository.save(bundle);
        eventPublisher.publishEvent(new BundleIndexEvent(saved));
        return toResponse(saved);
    }

    @Override
    public BundleResponse getBundle(long companyId, long bundleId, long ownerId) {
        assertOwnership(companyId, ownerId);
        return getBundle(companyId, bundleId);
    }

    @Override
    @Transactional
    public BundleResponse updateBundle(long companyId, long bundleId, long ownerId, UpdateBundleRequest request) {
        assertOwnership(companyId, ownerId);

        ProductBundle bundle = bundleRepository.findByIdAndCompanyId(bundleId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Bundle not found with id: " + bundleId));

        if (request.getName() != null) bundle.setName(request.getName());
        if (request.getDescription() != null) bundle.setDescription(request.getDescription());
        if (request.getThumbnailUrl() != null) bundle.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getCompareAtPrice() != null) bundle.setCompareAtPrice(request.getCompareAtPrice());
        if (request.getStatus() != null) bundle.setStatus(request.getStatus());
        if (request.getListed() != null) bundle.setListed(request.getListed());

        if (request.getItems() != null) {
            if (request.getItems().isEmpty()) {
                throw new BadRequestException("Bundle must contain at least one item");
            }
            validateNoDuplicates(request.getItems());
            bundle.getItems().clear();
            List<BundleItem> newItems = buildItems(bundle, request.getItems(), companyId);
            bundle.getItems().addAll(newItems);

            // Recompute price from new items if no explicit price is provided
            BigDecimal price = request.getPrice() != null
                    ? request.getPrice()
                    : computePrice(newItems);
            bundle.setPrice(price);
        } else if (request.getPrice() != null) {
            bundle.setPrice(request.getPrice());
        }

        ProductBundle saved = bundleRepository.save(bundle);
        eventPublisher.publishEvent(new BundleIndexEvent(saved));
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteBundle(long companyId, long bundleId, long ownerId) {
        assertOwnership(companyId, ownerId);

        ProductBundle bundle = bundleRepository.findByIdAndCompanyId(bundleId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Bundle not found with id: " + bundleId));

        bundleRepository.delete(bundle);
        eventPublisher.publishEvent(new BundleRemoveEvent(bundleId));
    }

    // --- Public read ---

    @Override
    public PagedResponse<BundleResponse> listBundles(long companyId, ProductStatus status, int page, int size) {
        if (size > 50) size = 50;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        // Public endpoint — always restrict to ACTIVE bundles regardless of requested status.
        return new PagedResponse<>(
                bundleRepository.findAllByCompanyIdAndStatus(companyId, ProductStatus.ACTIVE, pageable)
                        .map(this::toResponse));
    }

    @Override
    public BundleResponse getBundle(long companyId, long bundleId) {
        ProductBundle bundle = bundleRepository.findByIdAndCompanyId(bundleId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Bundle not found with id: " + bundleId));
        if (bundle.getStatus() != ProductStatus.ACTIVE) {
            throw new ResourceNotFoundException("Bundle not found with id: " + bundleId);
        }
        return toResponse(bundle);
    }

    // --- Helpers ---

    private void assertOwnership(long companyId, long ownerId) {
        companyRepository.findByIdAndOwnerId(companyId, ownerId)
                .orElseThrow(() -> new ForbiddenException("You do not own this company"));
    }

    private void validateNoDuplicates(List<BundleItemRequest> itemRequests) {
        Set<String> seen = new HashSet<>();
        for (BundleItemRequest ir : itemRequests) {
            String key = ir.getProductId() + ":" + ir.getVariantId();
            if (!seen.add(key)) {
                throw new BadRequestException("Duplicate product/variant combination in bundle items");
            }
        }
    }

    private List<BundleItem> buildItems(ProductBundle bundle, List<BundleItemRequest> itemRequests, long companyId) {
        if (itemRequests.size() > 10) {
            throw new BadRequestException("Bundle cannot have more than 10 products");
        }

        List<BundleItem> items = new ArrayList<>();
        for (BundleItemRequest ir : itemRequests) {
            Product product = productRepository.findByIdAndCompanyId(ir.getProductId(), companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + ir.getProductId()));

            ProductVariant variant = null;
            if (ir.getVariantId() != null) {
                variant = variantRepository.findByIdAndProductId(ir.getVariantId(), product.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + ir.getVariantId()));
            }

            BundleItem item = new BundleItem();
            item.setBundle(bundle);
            item.setProduct(product);
            item.setVariant(variant);
            item.setQuantity(ir.getQuantity());
            item.setDisplayOrder(ir.getDisplayOrder());
            items.add(item);
        }
        return items;
    }

    private BigDecimal computePrice(List<BundleItem> items) {
        return items.stream()
                .map(i -> {
                    BigDecimal unitPrice = i.getVariant() != null
                            ? i.getVariant().getPrice()
                            : i.getProduct().getPrice();
                    return unitPrice.multiply(BigDecimal.valueOf(i.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BundleResponse toResponse(ProductBundle b) {
        List<BundleItemResponse> items = b.getItems().stream()
                .map(i -> new BundleItemResponse(
                        i.getId(),
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getVariant() != null ? i.getVariant().getId() : null,
                        i.getVariant() != null ? i.getVariant().getSku() : null,
                        i.getVariant() != null ? buildVariantTitle(i.getVariant()) : null,
                        i.getQuantity(),
                        i.getDisplayOrder()))
                .toList();

        return new BundleResponse(
                b.getId(),
                b.getCompany().getId(),
                b.getName(),
                b.getDescription(),
                b.getThumbnailUrl(),
                b.getPrice(),
                b.getCompareAtPrice(),
                b.getCurrency(),
                b.getStatus().name(),
                b.isListed(),
                items,
                b.getCreatedAt(),
                b.getUpdatedAt());
    }

    private static String buildVariantTitle(ProductVariant variant) {
        String title = java.util.stream.Stream.of(variant.getOption1(), variant.getOption2(), variant.getOption3())
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.joining(" / "));
        return title.isBlank() ? null : title;
    }
}
