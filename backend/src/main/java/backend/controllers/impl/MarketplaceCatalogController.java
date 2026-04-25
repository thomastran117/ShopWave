package backend.controllers.impl;

import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.product.MarketplaceCatalogProductResponse;
import backend.dtos.responses.product.VendorStorefrontResponse;
import backend.exceptions.http.AppHttpException;
import backend.exceptions.http.InternalServerErrorException;
import backend.services.intf.ProductService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/marketplaces/{marketplaceId}/catalog")
public class MarketplaceCatalogController {

    private final ProductService productService;

    public MarketplaceCatalogController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public ResponseEntity<PagedResponse<MarketplaceCatalogProductResponse>> searchCatalog(
            @PathVariable long marketplaceId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        try {
            return ResponseEntity.ok(productService.searchMarketplaceCatalog(
                    marketplaceId, q, category, brand, minPrice, maxPrice,
                    featured, vendorId, page, size, sort, direction));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<MarketplaceCatalogProductResponse> getProduct(
            @PathVariable long marketplaceId,
            @PathVariable long productId) {
        try {
            return ResponseEntity.ok(productService.getMarketplaceProduct(marketplaceId, productId));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/vendors/{vendorId}/storefront")
    public ResponseEntity<VendorStorefrontResponse> getVendorStorefront(
            @PathVariable long marketplaceId,
            @PathVariable long vendorId) {
        try {
            return ResponseEntity.ok(productService.getVendorStorefront(marketplaceId, vendorId));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }
}
