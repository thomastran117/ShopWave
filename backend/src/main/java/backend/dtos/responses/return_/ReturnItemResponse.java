package backend.dtos.responses.return_;

import java.math.BigDecimal;

public record ReturnItemResponse(
        Long id,
        Long orderItemId,
        String productName,
        Long variantId,
        String variantTitle,
        int quantityReturned,
        BigDecimal unitPrice,
        boolean stockRestored,
        String condition    // null until inspectReturn() is called
) {}
