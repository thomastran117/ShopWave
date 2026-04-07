package backend.services.impl;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.dtos.requests.product.BatchCreateProductsRequest;
import backend.dtos.requests.product.BatchDeleteProductsRequest;
import backend.dtos.requests.product.CreateProductRequest;
import backend.dtos.requests.product.UpdateProductRequest;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.product.ProductResponse;
import backend.exceptions.http.ConflictException;
import backend.exceptions.http.ForbiddenException;
import backend.exceptions.http.ResourceNotFoundException;
import backend.models.core.Company;
import backend.models.core.Product;
import backend.models.enums.ProductStatus;
import backend.repositories.CompanyRepository;
import backend.repositories.ProductRepository;
import backend.repositories.specifications.ProductSpecification;
import backend.services.intf.ProductService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("name", "price", "createdAt", "stock");

    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;

    public ProductServiceImpl(ProductRepository productRepository, CompanyRepository companyRepository) {
        this.productRepository = productRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public PagedResponse<ProductResponse> searchProducts(
            long companyId,
            String q,
            String category,
            String brand,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean featured,
            ProductStatus status,
            Boolean listed,
            int page,
            int size,
            String sort,
            String direction) {

        assertCompanyExists(companyId);

        if (size > 50) size = 50;

        String sortField = (sort != null && SORTABLE_FIELDS.contains(sort)) ? sort : "createdAt";
        Sort.Direction sortDir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortField));

        return new PagedResponse<>(
                productRepository
                        .findAll(ProductSpecification.withFilters(companyId, q, category, brand, minPrice, maxPrice, featured, status, listed), pageable)
                        .map(this::toResponse)
        );
    }

    @Override
    public ProductResponse getProduct(long companyId, long productId) {
        assertCompanyExists(companyId);
        Product product = productRepository.findByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return toResponse(product);
    }

    @Override
    public List<ProductResponse> getProductsByIds(long companyId, List<Long> ids) {
        assertCompanyExists(companyId);
        return productRepository.findAllByIdInAndCompanyId(ids, companyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ProductResponse createProduct(long companyId, long ownerId, CreateProductRequest request) {
        Company company = companyRepository.findByIdAndOwnerId(companyId, ownerId)
                .orElseThrow(() -> new ForbiddenException("You do not own this company"));

        if (request.getSku() != null && !request.getSku().isBlank()
                && productRepository.existsBySkuAndCompanyId(request.getSku(), companyId)) {
            throw new ConflictException("A product with this SKU already exists in this company");
        }

        Product product = new Product();
        product.setCompany(company);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());
        product.setPrice(request.getPrice());
        product.setCompareAtPrice(request.getCompareAtPrice());
        product.setCurrency(request.getCurrency() != null ? request.getCurrency().toUpperCase() : "USD");
        product.setCategory(request.getCategory());
        product.setBrand(request.getBrand());
        product.setTags(request.getTags());
        product.setThumbnailUrl(request.getThumbnailUrl());
        product.setStock(request.getStock());
        product.setWeight(request.getWeight());
        product.setWeightUnit(request.getWeightUnit());
        product.setFeatured(request.isFeatured());
        product.setPurchasable(request.isPurchasable());
        product.setListed(request.isListed());
        product.setStatus(ProductStatus.DRAFT);

        return toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse updateProduct(long companyId, long productId, long ownerId, UpdateProductRequest request) {
        companyRepository.findByIdAndOwnerId(companyId, ownerId)
                .orElseThrow(() -> new ForbiddenException("You do not own this company"));

        Product product = productRepository.findByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (request.getSku() != null && !request.getSku().equals(product.getSku())) {
            if (productRepository.existsBySkuAndCompanyId(request.getSku(), companyId)) {
                throw new ConflictException("A product with this SKU already exists in this company");
            }
            product.setSku(request.getSku());
        }

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getCompareAtPrice() != null) product.setCompareAtPrice(request.getCompareAtPrice());
        if (request.getCurrency() != null) product.setCurrency(request.getCurrency().toUpperCase());
        if (request.getCategory() != null) product.setCategory(request.getCategory());
        if (request.getBrand() != null) product.setBrand(request.getBrand());
        if (request.getTags() != null) product.setTags(request.getTags());
        if (request.getThumbnailUrl() != null) product.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getStock() != null) product.setStock(request.getStock());
        if (request.getWeight() != null) product.setWeight(request.getWeight());
        if (request.getWeightUnit() != null) product.setWeightUnit(request.getWeightUnit());
        if (request.getStatus() != null) product.setStatus(request.getStatus());
        if (request.getFeatured() != null) product.setFeatured(request.getFeatured());
        if (request.getPurchasable() != null) product.setPurchasable(request.getPurchasable());
        if (request.getListed() != null) product.setListed(request.getListed());

        return toResponse(productRepository.save(product));
    }

    @Override
    public void deleteProduct(long companyId, long productId, long ownerId) {
        companyRepository.findByIdAndOwnerId(companyId, ownerId)
                .orElseThrow(() -> new ForbiddenException("You do not own this company"));

        Product product = productRepository.findByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        productRepository.delete(product);
    }

    @Override
    @Transactional
    public List<ProductResponse> batchCreateProducts(long companyId, long ownerId, BatchCreateProductsRequest request) {
        Company company = companyRepository.findByIdAndOwnerId(companyId, ownerId)
                .orElseThrow(() -> new ForbiddenException("You do not own this company"));

        List<ProductResponse> results = new java.util.ArrayList<>();

        for (CreateProductRequest req : request.getProducts()) {
            if (req.getSku() != null && !req.getSku().isBlank()
                    && productRepository.existsBySkuAndCompanyId(req.getSku(), companyId)) {
                throw new ConflictException("A product with SKU '" + req.getSku() + "' already exists in this company");
            }

            Product product = new Product();
            product.setCompany(company);
            product.setName(req.getName());
            product.setDescription(req.getDescription());
            product.setSku(req.getSku());
            product.setPrice(req.getPrice());
            product.setCompareAtPrice(req.getCompareAtPrice());
            product.setCurrency(req.getCurrency() != null ? req.getCurrency().toUpperCase() : "USD");
            product.setCategory(req.getCategory());
            product.setBrand(req.getBrand());
            product.setTags(req.getTags());
            product.setThumbnailUrl(req.getThumbnailUrl());
            product.setStock(req.getStock());
            product.setWeight(req.getWeight());
            product.setWeightUnit(req.getWeightUnit());
            product.setFeatured(req.isFeatured());
            product.setPurchasable(req.isPurchasable());
            product.setListed(req.isListed());
            product.setStatus(ProductStatus.DRAFT);

            results.add(toResponse(productRepository.save(product)));
        }

        return results;
    }

    @Override
    @Transactional
    public void batchDeleteProducts(long companyId, long ownerId, BatchDeleteProductsRequest request) {
        companyRepository.findByIdAndOwnerId(companyId, ownerId)
                .orElseThrow(() -> new ForbiddenException("You do not own this company"));

        List<Product> products = productRepository.findAllByIdInAndCompanyId(request.getIds(), companyId);

        if (products.size() != request.getIds().size()) {
            throw new ResourceNotFoundException("One or more products were not found in this company");
        }

        productRepository.deleteAll(products);
    }

    private void assertCompanyExists(long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("Company not found with id: " + companyId);
        }
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getCompany().getId(),
                product.getName(),
                product.getDescription(),
                product.getSku(),
                product.getPrice(),
                product.getCompareAtPrice(),
                product.getCurrency(),
                product.getCategory(),
                product.getBrand(),
                product.getTags(),
                product.getThumbnailUrl(),
                product.getStock(),
                product.getLowStockThreshold(),
                product.getWeight(),
                product.getWeightUnit(),
                product.getStatus().name(),
                product.isFeatured(),
                product.isPurchasable(),
                product.isListed(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
