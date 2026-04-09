package backend.dtos.requests.inventory;

import backend.models.enums.RestockStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateRestockRequest {

    private LocalDate expectedArrivalDate;

    private String supplierNote;

    /** Target status transition. Required when transitioning to RECEIVED. */
    private RestockStatus status;

    /** Required when status == RECEIVED. Must be >= 1. */
    private Integer receivedQty;
}
