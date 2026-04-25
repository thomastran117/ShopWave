package backend.services.intf;

import backend.dtos.requests.marketplace.VendorAdjustmentRequest;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.vendor.VendorAdjustmentResponse;
import backend.dtos.responses.vendor.VendorBalanceResponse;
import backend.dtos.responses.vendor.VendorPayoutResponse;
import backend.models.enums.PayoutStatus;

public interface VendorPayoutService {

    VendorBalanceResponse getBalance(long vendorId);

    PagedResponse<VendorPayoutResponse> listPayouts(long vendorId, PayoutStatus status, int page, int size);

    VendorPayoutResponse getPayoutDetail(long payoutId, long vendorId);

    /** Operator-triggered manual payout for a vendor with available balance. */
    VendorPayoutResponse triggerManualPayout(long vendorId, long marketplaceId, long operatorUserId);

    /** Called by operator to post a manual credit/debit to a vendor's balance. */
    VendorAdjustmentResponse createAdjustment(long vendorId, long operatorUserId, VendorAdjustmentRequest request);

    // -------------------------------------------------------------------------
    // Webhook callbacks (called from OrderController)
    // -------------------------------------------------------------------------

    /** Called when Stripe confirms a transfer has been paid to the vendor. */
    void handleTransferPaid(String stripeTransferId);

    /** Called when Stripe reports a transfer failure. */
    void handleTransferFailed(String stripeTransferId, String failureReason);
}
