package backend.controllers.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import backend.annotations.requireAuth.RequireAuth;
import backend.dtos.requests.product.AddProductImageRequest;
import backend.dtos.requests.product.BatchCreateProductsRequest;
import backend.dtos.requests.product.BatchDeleteProductsRequest;
import backend.dtos.requests.product.CreateProductRequest;
import backend.dtos.requests.product.ReorderProductImagesRequest;
import backend.dtos.requests.product.UpdateProductRequest;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.product.ProductImageResponse;
import backend.dtos.responses.product.ProductResponse;
import backend.exceptions.http.AppHttpException;
import backend.exceptions.http.InternalServerErrorException;
import backend.models.enums.ProductStatus;
import backend.services.intf.ProductService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/companies/{companyId}/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ProductResponse>> getProducts(
            @PathVariable long companyId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) Boolean listed,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        try {
            return ResponseEntity.ok(productService.searchProducts(companyId, q, category, brand, minPrice, maxPrice, featured, status, listed, page, size, sort, direction));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(
            @PathVariable long companyId,
            @PathVariable long id) {
        try {
            return ResponseEntity.ok(productService.getProduct(companyId, id));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("/batch-create")
    @RequireAuth
    public ResponseEntity<List<ProductResponse>> batchCreateProducts(
            @PathVariable long companyId,
            @Valid @RequestBody BatchCreateProductsRequest request) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.status(HttpStatus.CREATED).body(productService.batchCreateProducts(companyId, userId, request));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("/batch-delete")
    @RequireAuth
    public ResponseEntity<Void> batchDeleteProducts(
            @PathVariable long companyId,
            @Valid @RequestBody BatchDeleteProductsRequest request) {
        try {
            long userId = resolveUserId();
            productService.batchDeleteProducts(companyId, userId, request);
            return ResponseEntity.noContent().build();
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("/batch")
    @RequireAuth
    public ResponseEntity<List<ProductResponse>> getProductsByIds(
            @PathVariable long companyId,
            @RequestBody List<Long> ids) {
        try {
            return ResponseEntity.ok(productService.getProductsByIds(companyId, ids));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping
    @RequireAuth
    public ResponseEntity<ProductResponse> createProduct(
            @PathVariable long companyId,
            @Valid @RequestBody CreateProductRequest request) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(companyId, userId, request));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PatchMapping("/{id}")
    @RequireAuth
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable long companyId,
            @PathVariable long id,
            @Valid @RequestBody UpdateProductRequest request) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(productService.updateProduct(companyId, id, userId, request));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @DeleteMapping("/{id}")
    @RequireAuth
    public ResponseEntity<Void> deleteProduct(
            @PathVariable long companyId,
            @PathVariable long id) {
        try {
            long userId = resolveUserId();
            productService.deleteProduct(companyId, id, userId);
            return ResponseEntity.noContent().build();
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/{productId}/images")
    public ResponseEntity<List<ProductImageResponse>> getProductImages(
            @PathVariable long companyId,
            @PathVariable long productId) {
        try {
            return ResponseEntity.ok(productService.getProductImages(companyId, productId));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("/{productId}/images")
    @RequireAuth
    public ResponseEntity<ProductImageResponse> addProductImage(
            @PathVariable long companyId,
            @PathVariable long productId,
            @Valid @RequestBody AddProductImageRequest request) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(productService.addProductImage(companyId, productId, userId, request));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    @RequireAuth
    public ResponseEntity<Void> deleteProductImage(
            @PathVariable long companyId,
            @PathVariable long productId,
            @PathVariable long imageId) {
        try {
            long userId = resolveUserId();
            productService.deleteProductImage(companyId, productId, imageId, userId);
            return ResponseEntity.noContent().build();
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PatchMapping("/{productId}/images/reorder")
    @RequireAuth
    public ResponseEntity<List<ProductImageResponse>> reorderProductImages(
            @PathVariable long companyId,
            @PathVariable long productId,
            @Valid @RequestBody ReorderProductImagesRequest request) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(productService.reorderProductImages(companyId, productId, userId, request));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    private long resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((Number) auth.getPrincipal()).longValue();
    }
}
