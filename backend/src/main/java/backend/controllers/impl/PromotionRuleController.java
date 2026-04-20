package backend.controllers.impl;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import backend.annotations.requireAuth.RequireAuth;
import backend.dtos.requests.pricing.CreatePromotionRuleRequest;
import backend.dtos.requests.pricing.UpdatePromotionRuleRequest;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.pricing.PromotionRuleAnalyticsResponse;
import backend.dtos.responses.pricing.PromotionRuleResponse;
import backend.exceptions.http.AppHttpException;
import backend.exceptions.http.InternalServerErrorException;
import backend.services.intf.PricingReportService;
import backend.services.intf.PromotionRuleService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.Instant;

@RestController
@RequestMapping("/companies/{companyId}/promotion-rules")
public class PromotionRuleController {

    private final PromotionRuleService promotionRuleService;
    private final PricingReportService pricingReportService;

    public PromotionRuleController(
            PromotionRuleService promotionRuleService,
            PricingReportService pricingReportService) {
        this.promotionRuleService = promotionRuleService;
        this.pricingReportService = pricingReportService;
    }

    @GetMapping
    @RequireAuth
    public ResponseEntity<PagedResponse<PromotionRuleResponse>> listRules(
            @PathVariable long companyId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        try {
            return ResponseEntity.ok(promotionRuleService.listRules(companyId, resolveUserId(), page, size));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/{ruleId}")
    @RequireAuth
    public ResponseEntity<PromotionRuleResponse> getRule(
            @PathVariable long companyId,
            @PathVariable long ruleId) {
        try {
            return ResponseEntity.ok(promotionRuleService.getRule(companyId, ruleId, resolveUserId()));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping
    @RequireAuth
    public ResponseEntity<PromotionRuleResponse> createRule(
            @PathVariable long companyId,
            @Valid @RequestBody CreatePromotionRuleRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(promotionRuleService.createRule(companyId, resolveUserId(), request));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PatchMapping("/{ruleId}")
    @RequireAuth
    public ResponseEntity<PromotionRuleResponse> updateRule(
            @PathVariable long companyId,
            @PathVariable long ruleId,
            @Valid @RequestBody UpdatePromotionRuleRequest request) {
        try {
            return ResponseEntity.ok(promotionRuleService.updateRule(companyId, ruleId, resolveUserId(), request));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @DeleteMapping("/{ruleId}")
    @RequireAuth
    public ResponseEntity<Void> deleteRule(
            @PathVariable long companyId,
            @PathVariable long ruleId) {
        try {
            promotionRuleService.deleteRule(companyId, ruleId, resolveUserId());
            return ResponseEntity.noContent().build();
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/{ruleId}/analytics")
    @RequireAuth
    public ResponseEntity<PromotionRuleAnalyticsResponse> getRuleAnalytics(
            @PathVariable long companyId,
            @PathVariable long ruleId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        try {
            return ResponseEntity.ok(pricingReportService.getRuleAnalytics(
                    companyId, ruleId, resolveUserId(), from, to));
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
