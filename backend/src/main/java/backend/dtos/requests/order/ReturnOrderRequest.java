package backend.dtos.requests.order;

import java.util.List;

public record ReturnOrderRequest(
        /** Optional merchant note about the return. */
        String note,
        /**
         * Optional list of OrderItem IDs to return. When null or empty, all DELIVERED items are
         * returned. When provided, only the specified items are returned.
         */
        List<Long> itemIds,
        /** When true, restore stock for each returned item. */
        boolean restockItems,
        /** When true, issue a refund via the payment provider. */
        boolean issueRefund
) {}
