package backend.services.intf.pricing;

import backend.dtos.requests.marketplace.CreateCommissionPolicyRequest;
import backend.dtos.responses.marketplace.CommissionPolicyResponse;

import java.util.List;

public interface CommissionPolicyService {

    CommissionPolicyResponse createPolicy(long marketplaceId, long operatorUserId, CreateCommissionPolicyRequest request);

    void deletePolicy(long policyId, long marketplaceId, long operatorUserId);

    List<CommissionPolicyResponse> listPolicies(long marketplaceId, long operatorUserId);
}
