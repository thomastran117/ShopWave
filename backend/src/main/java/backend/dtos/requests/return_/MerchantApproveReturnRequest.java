package backend.dtos.requests.return_;

public record MerchantApproveReturnRequest(
        String merchantNote,
        /**
         * Refund amount in cents.
         * null  = auto-calculate from returned item unit prices × quantities.
         * 0     = intentionally waive the refund (no money back).
         * other = merchant-specified amount (supports restocking-fee deductions).
         */
        Long refundAmountOverrideCents
) {}
