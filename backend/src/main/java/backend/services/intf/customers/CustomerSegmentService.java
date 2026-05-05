package backend.services.intf.customers;

import backend.dtos.requests.segment.CreateCustomerSegmentRequest;
import backend.dtos.requests.segment.UpdateCustomerSegmentRequest;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.segment.CustomerSegmentResponse;

public interface CustomerSegmentService {
    PagedResponse<CustomerSegmentResponse> listSegments(int page, int size);
    CustomerSegmentResponse getSegment(long segmentId);
    CustomerSegmentResponse createSegment(CreateCustomerSegmentRequest request);
    CustomerSegmentResponse updateSegment(long segmentId, UpdateCustomerSegmentRequest request);
    void deleteSegment(long segmentId);

    /** Tag a user with a segment. No-op if already tagged. */
    void assignSegmentToUser(long userId, long segmentId);

    /** Untag a user from a segment. No-op if not tagged. */
    void removeSegmentFromUser(long userId, long segmentId);
}
