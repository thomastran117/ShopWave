package backend.services.intf;

import backend.dtos.requests.vendor.*;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.vendor.MarketplaceVendorResponse;
import backend.dtos.responses.vendor.StripeOnboardingLinkResponse;
import backend.dtos.responses.vendor.VendorDocumentResponse;
import backend.models.enums.VendorDocumentType;
import backend.models.enums.VendorStatus;

import java.util.List;

public interface VendorOnboardingService {

    /** Vendor applies to join a marketplace. Creates a MarketplaceVendor in DRAFT status. */
    MarketplaceVendorResponse applyToMarketplace(long marketplaceId, long requestingUserId, ApplyVendorRequest request);

    /** Vendor updates their profile (step 1 of onboarding). */
    MarketplaceVendorResponse updateProfile(long marketplaceId, long vendorId, long requestingUserId, UpdateVendorProfileRequest request);

    /** Vendor submits tax information (step 2 of onboarding). */
    MarketplaceVendorResponse submitTaxInfo(long marketplaceId, long vendorId, long requestingUserId, SubmitVendorTaxRequest request);

    /**
     * Creates or retrieves the vendor's Stripe Connect Express account and returns a
     * Stripe-hosted onboarding URL for the vendor to complete KYC / banking (step 3).
     */
    StripeOnboardingLinkResponse generateStripeOnboardingLink(
            long marketplaceId, long vendorId, long requestingUserId, GenerateStripeOnboardingLinkRequest request);

    /** Records a document upload (step 4). The actual file was uploaded directly to S3 by the client. */
    VendorDocumentResponse recordDocumentUpload(long marketplaceId, long vendorId, long requestingUserId,
                                                VendorDocumentType documentType, String s3Key);

    /** Vendor submits their application for review (moves from DRAFT/NEEDS_INFO → APPLIED). */
    MarketplaceVendorResponse submitForReview(long marketplaceId, long vendorId, long requestingUserId);

    // -------------------------------------------------------------------------
    // Operator actions
    // -------------------------------------------------------------------------

    MarketplaceVendorResponse approveVendor(long marketplaceId, long vendorId, long operatorUserId, VendorActionRequest request);

    MarketplaceVendorResponse rejectVendor(long marketplaceId, long vendorId, long operatorUserId, VendorActionRequest request);

    MarketplaceVendorResponse suspendVendor(long marketplaceId, long vendorId, long operatorUserId, VendorActionRequest request);

    MarketplaceVendorResponse reinstateVendor(long marketplaceId, long vendorId, long operatorUserId);

    MarketplaceVendorResponse requestMoreInfo(long marketplaceId, long vendorId, long operatorUserId, VendorActionRequest request);

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    MarketplaceVendorResponse getVendor(long marketplaceId, long vendorId);

    PagedResponse<MarketplaceVendorResponse> listVendors(long marketplaceId, VendorStatus status, int page, int size);

    /** Returns the vendor record for the authenticated user's company in the given marketplace. */
    MarketplaceVendorResponse getMyVendorRecord(long marketplaceId, long userId);

    List<VendorDocumentResponse> listDocuments(long marketplaceId, long vendorId, long requestingUserId);

    /** Syncs Stripe Connect account status into MarketplaceVendor (called from webhook or manually). */
    MarketplaceVendorResponse syncStripeConnectStatus(String stripeConnectAccountId);
}
