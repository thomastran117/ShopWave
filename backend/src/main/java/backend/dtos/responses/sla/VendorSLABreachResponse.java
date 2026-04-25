package backend.dtos.responses.sla;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class VendorSLABreachResponse {
    private long id;
    private long vendorId;
    private long policyId;
    private String metric;
    private double actualValue;
    private double threshold;
    private Instant detectedAt;
    private Instant resolvedAt;
    private String actionTaken;
    private Instant notificationSentAt;
    private Instant createdAt;
}
