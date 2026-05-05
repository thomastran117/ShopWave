package backend.services.intf.analytics;

import backend.dtos.responses.operations.CancellationMetricResponse;
import backend.dtos.responses.operations.DurationMetricResponse;
import backend.dtos.responses.operations.OperationsSummaryResponse;
import backend.dtos.responses.operations.StockoutMetricResponse;
import backend.dtos.responses.operations.SupplierLatenessMetricResponse;

/**
 * Aggregated SLA / operations metrics for a single merchant company. All
 * responses are read-only snapshots over a {@code lookbackDays} window. Caching
 * is internal to the implementation.
 */
public interface OperationsDashboardService {

    OperationsSummaryResponse        getSummary(long companyId, long ownerId, int lookbackDays);

    DurationMetricResponse           getFulfillmentMetric(long companyId, long ownerId, int lookbackDays);

    DurationMetricResponse           getRefundMetric(long companyId, long ownerId, int lookbackDays);

    DurationMetricResponse           getPickDelayMetric(long companyId, long ownerId, int lookbackDays);

    StockoutMetricResponse           getStockoutMetric(long companyId, long ownerId, int lookbackDays);

    SupplierLatenessMetricResponse   getSupplierLatenessMetric(long companyId, long ownerId, int lookbackDays);

    CancellationMetricResponse       getCancellationMetric(long companyId, long ownerId, int lookbackDays);
}
