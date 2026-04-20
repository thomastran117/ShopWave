package backend.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.dtos.requests.pricing.PricingQuoteRequest;
import backend.dtos.requests.pricing.PricingQuoteRequest.Item;
import backend.dtos.responses.pricing.AppliedPromotionResponse;
import backend.dtos.responses.pricing.LineBreakdownResponse;
import backend.dtos.responses.pricing.PricingQuoteResponse;
import backend.exceptions.http.BadRequestException;
import backend.exceptions.http.ResourceNotFoundException;
import backend.models.core.Product;
import backend.models.core.ProductVariant;
import backend.repositories.ProductRepository;
import backend.repositories.ProductVariantRepository;
import backend.repositories.UserRepository;
import backend.services.intf.PricingEngine;
import backend.services.intf.PricingQuoteService;
import backend.services.pricing.AppliedPromotion;
import backend.services.pricing.CartContext;
import backend.services.pricing.CartLine;
import backend.services.pricing.LineBreakdown;
import backend.services.pricing.PricingResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PricingQuoteServiceImpl implements PricingQuoteService {

    private final PricingEngine pricingEngine;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;

    public PricingQuoteServiceImpl(
            PricingEngine pricingEngine,
            ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            UserRepository userRepository) {
        this.pricingEngine = pricingEngine;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PricingQuoteResponse quote(PricingQuoteRequest request, Long userId) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("items must contain at least one line");
        }

        List<CartLine> cartLines = new ArrayList<>(request.getItems().size());
        for (int i = 0; i < request.getItems().size(); i++) {
            Item item = request.getItems().get(i);
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + item.getProductId()));

            BigDecimal unitPrice;
            Long variantId = item.getVariantId();
            if (variantId != null) {
                ProductVariant variant = variantRepository.findByIdAndProductId(variantId, product.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + variantId));
                unitPrice = variant.getPrice();
            } else {
                unitPrice = product.getPrice();
            }
            cartLines.add(new CartLine(
                    i,
                    product.getId(),
                    variantId,
                    item.getQuantity(),
                    unitPrice,
                    product.getCompany().getId()));
        }

        Set<Long> segmentIds = userId != null
                ? new HashSet<>(userRepository.findSegmentIdsByUserId(userId))
                : Set.of();

        String currency = request.getCurrency() != null ? request.getCurrency() : "USD";
        CartContext ctx = new CartContext(
                cartLines,
                userId,
                segmentIds,
                currency,
                request.getCouponCode(),
                request.getShippingAmount(),
                Instant.now());

        PricingResult result = pricingEngine.quote(ctx);
        return toResponse(result, currency);
    }

    private PricingQuoteResponse toResponse(PricingResult r, String currency) {
        List<LineBreakdownResponse> lines = new ArrayList<>(r.lines().size());
        for (LineBreakdown lb : r.lines()) {
            lines.add(new LineBreakdownResponse(
                    lb.index(),
                    lb.productId(),
                    lb.variantId(),
                    lb.quantity(),
                    lb.unitBasePrice(),
                    lb.savings(),
                    lb.effectiveLineTotal(),
                    lb.appliedRuleIds()));
        }
        List<AppliedPromotionResponse> applied = new ArrayList<>(r.appliedPromotions().size());
        for (AppliedPromotion ap : r.appliedPromotions()) {
            applied.add(new AppliedPromotionResponse(
                    ap.ruleId(), ap.name(), ap.ruleType(), ap.savings(), ap.fundedByCompanyId()));
        }
        return new PricingQuoteResponse(
                lines,
                applied,
                r.subtotal(),
                r.promotionSavings(),
                r.couponSavings(),
                r.appliedCouponCode(),
                r.shippingAmount(),
                r.finalTotal(),
                currency,
                r.warnings());
    }
}
