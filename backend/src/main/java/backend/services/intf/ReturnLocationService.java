package backend.services.intf;

import backend.dtos.requests.return_.CreateReturnLocationRequest;
import backend.dtos.requests.return_.UpdateReturnLocationRequest;
import backend.dtos.responses.return_.ReturnLocationResponse;

import java.util.List;

public interface ReturnLocationService {

    /** Merchant adds a new return location. If primary=true, any existing primary is cleared first. */
    ReturnLocationResponse createReturnLocation(long companyId, long ownerId, CreateReturnLocationRequest request);

    /** Returns all return locations for a company (merchant only). */
    List<ReturnLocationResponse> getReturnLocations(long companyId, long ownerId);

    /** Updates an existing return location. If setting primary=true, clears existing primary first. */
    ReturnLocationResponse updateReturnLocation(long locationId, long companyId, long ownerId, UpdateReturnLocationRequest request);

    /**
     * Deletes a return location. Fails if it is the only location for the company —
     * at least one location must remain configured.
     */
    void deleteReturnLocation(long locationId, long companyId, long ownerId);
}
