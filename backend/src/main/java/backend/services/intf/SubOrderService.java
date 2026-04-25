package backend.services.intf;

import backend.dtos.requests.order.CancelSubOrderRequest;
import backend.dtos.requests.order.ShipSubOrderRequest;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.order.CommissionRecordResponse;
import backend.dtos.responses.order.SubOrderResponse;
import backend.models.enums.SubOrderStatus;

public interface SubOrderService {

    /** Returns paginated sub-orders for a vendor, optionally filtered by status. */
    PagedResponse<SubOrderResponse> listVendorSubOrders(long marketplaceVendorId, SubOrderStatus status, int page, int size);

    SubOrderResponse getSubOrder(long subOrderId, long marketplaceVendorId);

    SubOrderResponse markPacked(long subOrderId, long marketplaceVendorId);

    SubOrderResponse markShipped(long subOrderId, long marketplaceVendorId, ShipSubOrderRequest request);

    SubOrderResponse markDelivered(long subOrderId, long marketplaceVendorId);

    SubOrderResponse cancelSubOrder(long subOrderId, long marketplaceVendorId, CancelSubOrderRequest request);

    CommissionRecordResponse getCommissionRecord(long subOrderId, long marketplaceVendorId);
}
