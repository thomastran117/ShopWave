package backend.repositories.projections;

import java.time.LocalDate;

public interface DailyDemandProjection {
    Long getProductId();
    LocalDate getDay();
    Long getUnits();
}
