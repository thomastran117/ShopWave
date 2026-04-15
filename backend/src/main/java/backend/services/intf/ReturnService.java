package backend.services.intf;

import backend.dtos.requests.return_.BuyerInitiateReturnRequest;
import backend.dtos.requests.return_.InspectReturnRequest;
import backend.dtos.requests.return_.MerchantApproveReturnRequest;
import backend.dtos.requests.return_.MerchantInitiateReturnRequest;
import backend.dtos.requests.return_.MerchantRejectReturnRequest;
import backend.dtos.responses.return_.ReturnResponse;

import java.util.List;

public interface ReturnService {

    // -------------------------------------------------------------------------
    // Buyer-facing
    // -------------------------------------------------------------------------

    /**
     * Buyer submits a return request. Order must be DELIVERED.
     * Return is created in REQUESTED status for merchant review.
     */
    ReturnResponse requestReturn(long orderId, long buyerUserId, BuyerInitiateReturnRequest request);

    /** Buyer views all return requests for a specific order. */
    List<ReturnResponse> getReturnsByOrder(long orderId, long buyerUserId);

    // -------------------------------------------------------------------------
    // Merchant-facing
    // -------------------------------------------------------------------------

    /** Merchant views all return requests for a specific order scoped to their company's items. */
    List<ReturnResponse> getCompanyReturnsByOrder(long orderId, long companyId, long ownerId);

    /** Merchant retrieves a single return scoped to their company. */
    ReturnResponse getCompanyReturn(long returnId, long companyId, long ownerId);

    /**
     * Merchant approves a REQUESTED return: marks items RETURNED and issues refund.
     * Transitions Return to APPROVED (awaiting physical inspection). Stock is NOT
     * restored here — that happens in inspectReturn() once items are received.
     * Transitions to FAILED if the Stripe refund call errors.
     */
    ReturnResponse approveReturn(long returnId, long companyId, long ownerId, MerchantApproveReturnRequest request);

    /**
     * Merchant records per-item condition after physical inspection of returned goods
     * and decides whether to restock each item. Transitions Return from APPROVED to COMPLETED.
     */
    ReturnResponse inspectReturn(long returnId, long companyId, long ownerId, InspectReturnRequest request);

    /**
     * Merchant rejects a REQUESTED return. Transitions to REJECTED.
     * No stock or refund changes are made.
     */
    ReturnResponse rejectReturn(long returnId, long companyId, long ownerId, MerchantRejectReturnRequest request);

    /**
     * Merchant directly initiates a return without a prior buyer request.
     * Creates the Return entity and immediately processes stock + refund in a single transaction.
     * Valid for orders in DELIVERED or SHIPPED status.
     */
    ReturnResponse merchantInitiateReturn(long orderId, long companyId, long ownerId, MerchantInitiateReturnRequest request);

    // -------------------------------------------------------------------------
    // Webhook
    // -------------------------------------------------------------------------

    /**
     * Called when Stripe fires a charge.refunded or refund.updated event.
     * Looks up the Return by stripeRefundId, updates RefundStatus, syncs
     * order.refundedAmountCents, and recomputes OrderStatus (may tip to REFUNDED).
     */
    void handleRefundWebhookEvent(String stripeRefundId, String stripeStatus, long amountCents);
}
