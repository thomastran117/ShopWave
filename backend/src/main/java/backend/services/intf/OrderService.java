package backend.services.intf;

import backend.dtos.requests.order.CreateOrderRequest;
import backend.dtos.requests.order.ReturnOrderRequest;
import backend.dtos.requests.order.ShipOrderRequest;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.order.CompanyOrderResponse;
import backend.dtos.responses.order.OrderResponse;
import backend.models.enums.OrderStatus;

public interface OrderService {
    OrderResponse createOrder(long userId, CreateOrderRequest request);
    OrderResponse getOrder(long orderId, long userId);
    PagedResponse<OrderResponse> getOrders(long userId, OrderStatus status, int page, int size, String sort, String direction);
    OrderResponse cancelOrder(long orderId, long userId);
    void handlePaymentSuccess(String paymentIntentId);
    void handlePaymentFailure(String paymentIntentId);
    PagedResponse<CompanyOrderResponse> getCompanyOrders(long companyId, long ownerId, OrderStatus status, int page, int size);
    CompanyOrderResponse getCompanyOrder(long companyId, long orderId, long ownerId);

    void fulfillPendingBackorders(long productId, Long variantId, int availableQty, Long fulfillmentLocationId);

    // -------------------------------------------------------------------------
    // Merchant fulfillment transitions
    // -------------------------------------------------------------------------

    /** Transitions PAID order to PACKED — marks all PENDING items as PACKED. */
    CompanyOrderResponse markAsPacked(long companyId, long orderId, long ownerId);

    /** Transitions PACKED (or PARTIALLY_FULFILLED) order items to SHIPPED; records tracking info. */
    CompanyOrderResponse markAsShipped(long companyId, long orderId, long ownerId, ShipOrderRequest request);

    /** Transitions SHIPPED (or PARTIALLY_FULFILLED) order to DELIVERED. */
    CompanyOrderResponse markAsDelivered(long companyId, long orderId, long ownerId);

    /** Processes a return for a DELIVERED (or SHIPPED) order — optionally restocks and refunds. */
    CompanyOrderResponse initiateReturn(long companyId, long orderId, long ownerId, ReturnOrderRequest request);
}
