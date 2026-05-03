package backend.services.intf.vendors;

import backend.dtos.requests.sla.CreateSLAPolicyRequest;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.sla.VendorSLABreachResponse;
import backend.dtos.responses.sla.VendorSLAMetricResponse;
import backend.dtos.responses.sla.VendorSLAPolicyResponse;

import java.util.List;

public interface VendorSLAService {

    // Policy management (operator)
    VendorSLAPolicyResponse createPolicy(long marketplaceId, long operatorUserId, CreateSLAPolicyRequest request);
    VendorSLAPolicyResponse getActivePolicy(long marketplaceId);
    List<VendorSLAPolicyResponse> listPolicies(long marketplaceId);

    // Metrics (vendor self-service + operator)
    PagedResponse<VendorSLAMetricResponse> listMetrics(long marketplaceId, long vendorId, long actorUserId, int page, int size);
    VendorSLAMetricResponse getLatestMetric(long marketplaceId, long vendorId, long actorUserId);

    // Breaches (vendor self-service + operator)
    PagedResponse<VendorSLABreachResponse> listBreaches(long marketplaceId, long vendorId, long actorUserId, int page, int size);
    VendorSLABreachResponse resolveBreach(long breachId, long operatorUserId, long marketplaceId);
}
