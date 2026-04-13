package backend.services.impl;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.dtos.requests.discount.CreateDiscountRequest;
import backend.dtos.requests.discount.UpdateDiscountRequest;
import backend.dtos.responses.discount.DiscountResponse;
import backend.dtos.responses.general.PagedResponse;
import backend.exceptions.http.BadRequestException;
import backend.exceptions.http.ForbiddenException;
import backend.exceptions.http.ResourceNotFoundException;
import backend.models.core.Discount;
import backend.models.core.Product;
import backend.models.enums.DiscountStatus;
import backend.models.enums.DiscountType;
import backend.repositories.CompanyRepository;
import backend.repositories.DiscountRepository;
import backend.repositories.ProductRepository;
import backend.services.intf.DiscountService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;
    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;

    public DiscountServiceImpl(
            DiscountRepository discountRepository,
            CompanyRepository companyRepository,
            ProductRepository productRepository) {
        this.discountRepository = discountRepository;
        this.companyRepository = companyRepository;
        this.productRepository = productRepository;
    }

    @Override
    public PagedResponse<DiscountResponse> listDiscounts(long companyId, long ownerId, int page, int size) {
        companyRepository.findByIdAndOwnerId(companyId, ownerId)
                .orElseThrow(() -> new ForbiddenException("You do not own this company"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return new PagedResponse<>(
                discountRepository.findAllByCompanyId(companyId, pageable).map(this::toResponse));
    }

    @Override
    public DiscountResponse getDiscount(long companyId, long discountId, long ownerId) {
        companyRepository.findByIdAndOwnerId(companyId, ownerId)
                .orElseThrow(() -> new ForbiddenException("You do not own this company"));

        Discount discount = discountRepository.findByIdAndCompanyId(discountId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + discountId));
        return toResponse(discount);
    }

    @Override
    @Transactional
    public DiscountResponse createDiscount(long companyId, long ownerId, CreateDiscountRequest request) {
        companyRepository.findByIdAndOwnerId(companyId, ownerId)
                .orElseThrow(() -> new ForbiddenException("You do not own this company"));

        DiscountType type = parseType(request.getType());
        validateValue(type, request.getValue());
        validateDates(request.getStartDate(), request.getEndDate());

        Set<Product> products = resolveAndValidateProducts(request.getProductIds(), companyId);

        Discount discount = new Discount();
        discount.setCompany(companyRepository.getReferenceById(companyId));
        discount.setName(request.getName());
        discount.setType(type);
        discount.setValue(request.getValue());
        discount.setStartDate(request.getStartDate());
        discount.setEndDate(request.getEndDate());
        discount.setProducts(products);

        return toResponse(discountRepository.save(discount));
    }

    @Override
    @Transactional
    public DiscountResponse updateDiscount(long companyId, long discountId, long ownerId, UpdateDiscountRequest request) {
        companyRepository.findByIdAndOwnerId(companyId, ownerId)
                .orElseThrow(() -> new ForbiddenException("You do not own this company"));

        Discount discount = discountRepository.findByIdAndCompanyId(discountId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + discountId));

        if (request.getName() != null) discount.setName(request.getName());

        DiscountType effectiveType = discount.getType();
        BigDecimal effectiveValue = discount.getValue();

        if (request.getType() != null) {
            effectiveType = parseType(request.getType());
            discount.setType(effectiveType);
        }
        if (request.getValue() != null) {
            effectiveValue = request.getValue();
            discount.setValue(effectiveValue);
        }

        // Re-validate if either type or value changed
        if (request.getType() != null || request.getValue() != null) {
            validateValue(effectiveType, effectiveValue);
        }

        if (request.getStatus() != null) {
            if ("EXPIRED".equalsIgnoreCase(request.getStatus())) {
                throw new BadRequestException("Status 'EXPIRED' cannot be set directly — it is computed from endDate");
            }
            if ("ACTIVE".equalsIgnoreCase(request.getStatus())) {
                discount.setStatus(DiscountStatus.ACTIVE);
            } else if ("DISABLED".equalsIgnoreCase(request.getStatus())) {
                discount.setStatus(DiscountStatus.DISABLED);
            } else {
                throw new BadRequestException("Invalid status '" + request.getStatus() + "'. Must be ACTIVE or DISABLED");
            }
        }

        if (request.getStartDate() != null || request.getEndDate() != null) {
            Instant newStart = request.getStartDate() != null ? request.getStartDate() : discount.getStartDate();
            Instant newEnd   = request.getEndDate()   != null ? request.getEndDate()   : discount.getEndDate();
            validateDates(newStart, newEnd);
            if (request.getStartDate() != null) discount.setStartDate(request.getStartDate());
            if (request.getEndDate()   != null) discount.setEndDate(request.getEndDate());
        }

        if (request.getProductIds() != null) {
            discount.setProducts(resolveAndValidateProducts(request.getProductIds(), companyId));
        }

        return toResponse(discountRepository.save(discount));
    }

    @Override
    @Transactional
    public void deleteDiscount(long companyId, long discountId, long ownerId) {
        companyRepository.findByIdAndOwnerId(companyId, ownerId)
                .orElseThrow(() -> new ForbiddenException("You do not own this company"));

        Discount discount = discountRepository.findByIdAndCompanyId(discountId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + discountId));

        // Clear join-table rows first so Hibernate removes them cleanly
        discount.getProducts().clear();
        discountRepository.delete(discount);
    }

    // --- Private helpers ---

    private DiscountResponse toResponse(Discount d) {
        Instant now = Instant.now();
        DiscountStatus effectiveStatus = (d.getEndDate() != null && d.getEndDate().isBefore(now))
                ? DiscountStatus.EXPIRED
                : d.getStatus();

        List<Long> productIds = d.getProducts().stream()
                .map(Product::getId)
                .sorted()
                .toList();

        return new DiscountResponse(
                d.getId(),
                d.getCompany().getId(),
                d.getName(),
                d.getType(),
                d.getValue(),
                effectiveStatus,
                d.getStartDate(),
                d.getEndDate(),
                productIds,
                d.getCreatedAt(),
                d.getUpdatedAt());
    }

    private DiscountType parseType(String raw) {
        try {
            return DiscountType.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("Invalid discount type '" + raw + "'. Must be PERCENTAGE or FIXED_AMOUNT");
        }
    }

    private void validateValue(DiscountType type, BigDecimal value) {
        if (type == DiscountType.PERCENTAGE && value.compareTo(new BigDecimal("100")) > 0) {
            throw new BadRequestException("Percentage discount value cannot exceed 100");
        }
    }

    private void validateDates(Instant startDate, Instant endDate) {
        if (startDate != null && endDate != null && !endDate.isAfter(startDate)) {
            throw new BadRequestException("endDate must be strictly after startDate");
        }
    }

    private Set<Product> resolveAndValidateProducts(List<Long> ids, long companyId) {
        List<Long> deduped = new ArrayList<>(new HashSet<>(ids));
        List<Product> found = productRepository.findAllByIdInAndCompanyId(deduped, companyId);
        if (found.size() != deduped.size()) {
            throw new BadRequestException("One or more product IDs are invalid or do not belong to this company");
        }
        return new HashSet<>(found);
    }
}
