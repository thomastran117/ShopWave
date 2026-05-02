package backend.controllers.impl.analytics;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import backend.annotations.requireAuth.RequireAuth;
import backend.dtos.responses.analytics.HotProductsResponse;
import backend.exceptions.http.AppHttpException;
import backend.exceptions.http.InternalServerErrorException;
import backend.services.intf.inventory.DemandService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/companies/{companyId}/analytics")
public class AnalyticsController {

    private final DemandService demandService;

    public AnalyticsController(DemandService demandService) {
        this.demandService = demandService;
    }

    /**
     * Returns the top products by purchase velocity within the requested time window.
     *
     * window — "1h" (last 60 minutes) or "24h" (last 24 hours). Default: "1h".
     * limit  — number of products to return, 1–50. Default: 20.
     *
     * Each entry includes:
     *   - velocityPerHour: units sold per hour within the window
     *   - accelerationRatio: for the 1h window, how much faster the product
     *     is selling compared to its own 24h average (> 1.0 = trending up)
     */
    @GetMapping("/hot-products")
    @RequireAuth
    public ResponseEntity<HotProductsResponse> getHotProducts(
            @PathVariable long companyId,
            @RequestParam(defaultValue = "1h") String window,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(demandService.getHotProducts(companyId, userId, window, limit));
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
