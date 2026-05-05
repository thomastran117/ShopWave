package backend.dtos.responses.forecasting;

public record SeasonalPrepResponse(
        long productId,
        String productName,
        String sku,
        Integer currentStock,
        double avgDailyLast28,
        double avgDailyYoY,
        double yoyRatio,
        Trend trend
) {
    public enum Trend {
        RAMPING_UP,
        COOLING_DOWN,
        STABLE
    }
}
