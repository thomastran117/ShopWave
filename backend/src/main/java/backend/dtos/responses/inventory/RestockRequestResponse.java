package backend.dtos.responses.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class RestockRequestResponse {
    private Long id;
    private Long companyId;
    private Long productId;
    private String productName;
    private Long variantId;
    private String variantSku;
    private Long locationId;
    private String locationName;
    private int requestedQty;
    private Integer receivedQty;
    private LocalDate expectedArrivalDate;
    private String status;
    private String supplierNote;
    private Long createdByUserId;
    private Instant createdAt;
    private Instant updatedAt;
}
