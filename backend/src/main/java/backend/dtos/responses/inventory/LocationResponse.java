package backend.dtos.responses.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class LocationResponse {
    private Long id;
    private Long companyId;
    private String name;
    private String code;
    private String address;
    private String city;
    private String country;
    private boolean active;
    private int displayOrder;
    private Double latitude;
    private Double longitude;
    private BigDecimal fulfillmentCost;
    private Instant createdAt;
    private Instant updatedAt;
}
