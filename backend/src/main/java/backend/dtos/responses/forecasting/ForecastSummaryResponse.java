package backend.dtos.responses.forecasting;

import java.time.Instant;
import java.util.List;

public record ForecastSummaryResponse(
        long companyId,
        int windowDays,
        Instant computedAt,
        int productCount,
        int productsNeedingReorder,
        int productsWithImminentStockout,
        List<ProductForecastResponse> items
) {}
