package backend.services.intf.subscriptions;

import backend.dtos.requests.subscription.CreateSubscriptionRequest;
import backend.dtos.requests.subscription.UpdateSubscriptionRequest;
import backend.dtos.responses.subscription.SavedPaymentMethodResponse;
import backend.dtos.responses.subscription.SetupIntentResponse;
import backend.dtos.responses.subscription.SubscriptionResponse;

import java.util.List;

/**
 * Customer-facing recurring-order lifecycle. Stripe is the source of truth for
 * billing; this service mirrors local state and translates customer actions
 * (pause / skip / cancel / change) into Stripe API calls.
 */
public interface SubscriptionService {

    // -------------------------------------------------------------------------
    // Saved payment methods (cards reusable across subscription renewals)
    // -------------------------------------------------------------------------

    /** Creates a SetupIntent the client uses to save a card for off-session reuse. */
    SetupIntentResponse createSetupIntent(long userId);

    List<SavedPaymentMethodResponse> listPaymentMethods(long userId);

    void detachPaymentMethod(long userId, long savedPaymentMethodId);

    // -------------------------------------------------------------------------
    // Subscription lifecycle
    // -------------------------------------------------------------------------

    SubscriptionResponse create(long userId, CreateSubscriptionRequest request);

    SubscriptionResponse get(long userId, long subscriptionId);

    List<SubscriptionResponse> listForUser(long userId);

    /** Edits quantity, interval, or swaps the product. Any non-null field is applied. */
    SubscriptionResponse update(long userId, long subscriptionId, UpdateSubscriptionRequest request);

    SubscriptionResponse pause(long userId, long subscriptionId);

    SubscriptionResponse resume(long userId, long subscriptionId);

    /** Skips the next scheduled charge by advancing the billing anchor by one cycle. */
    SubscriptionResponse skipNext(long userId, long subscriptionId);

    /** Cancels the subscription. {@code atPeriodEnd=true} keeps service through the current cycle. */
    SubscriptionResponse cancel(long userId, long subscriptionId, boolean atPeriodEnd);

    // -------------------------------------------------------------------------
    // Stripe webhook handlers
    // -------------------------------------------------------------------------

    /** Re-syncs status, period, and cancellation flags from Stripe. */
    void handleSubscriptionUpdated(String stripeSubscriptionId);

    /** Spawns a fulfillment Order for a paid invoice (idempotent on invoice ID). */
    void handleInvoicePaid(String stripeInvoiceId, String stripeSubscriptionId, long amountPaidCents);

    /** Marks the subscription PAST_DUE and notifies the customer. */
    void handleInvoicePaymentFailed(String stripeInvoiceId, String stripeSubscriptionId);

    /** Persists a saved payment method when a SetupIntent succeeds. */
    void handleSetupIntentSucceeded(String stripeCustomerId, String stripePaymentMethodId);
}
