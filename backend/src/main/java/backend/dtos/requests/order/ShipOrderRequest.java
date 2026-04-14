package backend.dtos.requests.order;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ShipOrderRequest(
        /** Required carrier tracking number. */
        @NotBlank String trackingNumber,
        /** Optional carrier name (e.g. "UPS", "FedEx", "USPS"). */
        String carrier,
        /** Optional merchant note recorded on the order. */
        String note,
        /**
         * Optional list of OrderItem IDs to ship. When null or empty, all PACKED items are
         * transitioned to SHIPPED. When provided, only the specified items are shipped,
         * enabling partial shipments that set order status to PARTIALLY_FULFILLED.
         */
        List<Long> itemIds
) {}
