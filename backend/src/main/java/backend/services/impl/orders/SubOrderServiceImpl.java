package backend.services.impl.orders;

import backend.dtos.requests.order.CancelSubOrderRequest;
import backend.dtos.requests.order.ShipSubOrderRequest;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.order.CommissionRecordResponse;
import backend.dtos.responses.order.OrderItemResponse;
import backend.dtos.responses.order.SubOrderResponse;
import backend.exceptions.http.BadRequestException;
import backend.exceptions.http.ResourceNotFoundException;
import backend.models.core.CommissionRecord;
import backend.models.core.OrderItem;
import backend.models.core.SubOrder;
import backend.models.enums.FulfillmentStatus;
import backend.models.enums.SubOrderStatus;
import backend.repositories.CommissionRecordRepository;
import backend.repositories.OrderItemRepository;
import backend.repositories.SubOrderRepository;
import backend.services.intf.orders.SubOrderService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class SubOrderServiceImpl implements SubOrderService {

    private final SubOrderRepository subOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CommissionRecordRepository commissionRecordRepository;

    public SubOrderServiceImpl(
            SubOrderRepository subOrderRepository,
            OrderItemRepository orderItemRepository,
            CommissionRecordRepository commissionRecordRepository) {
        this.subOrderRepository = subOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.commissionRecordRepository = commissionRecordRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SubOrderResponse> listVendorSubOrders(long marketplaceVendorId, SubOrderStatus status, int page, int size) {
        if (size > 50) size = 50;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SubOrder> results = status != null
                ? subOrderRepository.findByMarketplaceVendorIdAndStatus(marketplaceVendorId, status, pageable)
                : subOrderRepository.findByMarketplaceVendorId(marketplaceVendorId, pageable);
        return new PagedResponse<>(results.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public SubOrderResponse getSubOrder(long subOrderId, long marketplaceVendorId) {
        return toResponse(resolveVendorSubOrder(subOrderId, marketplaceVendorId));
    }

    @Override
    @Transactional
    public SubOrderResponse markPacked(long subOrderId, long marketplaceVendorId) {
        SubOrder subOrder = resolveVendorSubOrder(subOrderId, marketplaceVendorId);
        if (subOrder.getStatus() != SubOrderStatus.PENDING) {
            throw new BadRequestException("Sub-order must be PENDING to mark as packed");
        }
        subOrder.setStatus(SubOrderStatus.PACKED);
        subOrder.setPackedAt(Instant.now());

        orderItemRepository.findAllBySubOrderId(subOrderId).forEach(item -> {
            if (item.getFulfillmentStatus() == FulfillmentStatus.PENDING) {
                item.setFulfillmentStatus(FulfillmentStatus.PACKED);
                orderItemRepository.save(item);
            }
        });

        return toResponse(subOrderRepository.save(subOrder));
    }

    @Override
    @Transactional
    public SubOrderResponse markShipped(long subOrderId, long marketplaceVendorId, ShipSubOrderRequest request) {
        SubOrder subOrder = resolveVendorSubOrder(subOrderId, marketplaceVendorId);
        if (subOrder.getStatus() != SubOrderStatus.PACKED) {
            throw new BadRequestException("Sub-order must be PACKED before marking as shipped");
        }
        subOrder.setStatus(SubOrderStatus.SHIPPED);
        subOrder.setShippedAt(Instant.now());
        subOrder.setTrackingNumber(request.getTrackingNumber());
        subOrder.setCarrier(request.getCarrier());
        if (request.getFulfillmentNote() != null) {
            subOrder.setFulfillmentNote(request.getFulfillmentNote());
        }

        orderItemRepository.findAllBySubOrderId(subOrderId).forEach(item -> {
            if (item.getFulfillmentStatus() == FulfillmentStatus.PACKED) {
                item.setFulfillmentStatus(FulfillmentStatus.SHIPPED);
                orderItemRepository.save(item);
            }
        });

        return toResponse(subOrderRepository.save(subOrder));
    }

    @Override
    @Transactional
    public SubOrderResponse markDelivered(long subOrderId, long marketplaceVendorId) {
        SubOrder subOrder = resolveVendorSubOrder(subOrderId, marketplaceVendorId);
        if (subOrder.getStatus() != SubOrderStatus.SHIPPED) {
            throw new BadRequestException("Sub-order must be SHIPPED before marking as delivered");
        }
        subOrder.setStatus(SubOrderStatus.DELIVERED);
        subOrder.setDeliveredAt(Instant.now());

        orderItemRepository.findAllBySubOrderId(subOrderId).forEach(item -> {
            if (item.getFulfillmentStatus() == FulfillmentStatus.SHIPPED) {
                item.setFulfillmentStatus(FulfillmentStatus.DELIVERED);
                orderItemRepository.save(item);
            }
        });

        return toResponse(subOrderRepository.save(subOrder));
    }

    @Override
    @Transactional
    public SubOrderResponse cancelSubOrder(long subOrderId, long marketplaceVendorId, CancelSubOrderRequest request) {
        SubOrder subOrder = resolveVendorSubOrder(subOrderId, marketplaceVendorId);
        if (subOrder.getStatus() == SubOrderStatus.SHIPPED
                || subOrder.getStatus() == SubOrderStatus.DELIVERED
                || subOrder.getStatus() == SubOrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot cancel a sub-order in status " + subOrder.getStatus());
        }
        subOrder.setStatus(SubOrderStatus.CANCELLED);
        subOrder.setCancelledAt(Instant.now());
        subOrder.setCancellationReason(request.getReason());
        return toResponse(subOrderRepository.save(subOrder));
    }

    @Override
    @Transactional(readOnly = true)
    public CommissionRecordResponse getCommissionRecord(long subOrderId, long marketplaceVendorId) {
        resolveVendorSubOrder(subOrderId, marketplaceVendorId);
        CommissionRecord record = commissionRecordRepository.findBySubOrderId(subOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Commission record not yet available for this sub-order"));
        return toCommissionResponse(record);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private SubOrder resolveVendorSubOrder(long subOrderId, long marketplaceVendorId) {
        return subOrderRepository.findByIdAndMarketplaceVendorId(subOrderId, marketplaceVendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sub-order not found"));
    }

    private SubOrderResponse toResponse(SubOrder s) {
        List<OrderItem> items = orderItemRepository.findAllBySubOrderId(s.getId());
        List<OrderItemResponse> itemResponses = items.stream().map(this::toItemResponse).toList();
        return new SubOrderResponse(
                s.getId(),
                s.getOrder().getId(),
                s.getMarketplaceVendor().getId(),
                s.getMarketplaceId(),
                s.getMarketplaceVendor().getVendorCompany().getName(),
                s.getStatus().name(),
                s.getSubtotal(),
                s.getTotalAmount(),
                s.getCurrency(),
                s.getCommissionAmount(),
                s.getNetVendorAmount(),
                s.getTrackingNumber(),
                s.getCarrier(),
                s.getFulfillmentNote(),
                s.getCancellationReason(),
                s.getPaidAt(),
                s.getPackedAt(),
                s.getShippedAt(),
                s.getDeliveredAt(),
                s.getCancelledAt(),
                s.getCreatedAt(),
                s.getUpdatedAt(),
                itemResponses
        );
    }

    private OrderItemResponse toItemResponse(OrderItem i) {
        return new OrderItemResponse(
                i.getId(),
                i.getProduct() != null ? i.getProduct().getId() : null,
                i.getProductName(),
                i.getVariant() != null ? i.getVariant().getId() : null,
                i.getVariantTitle(),
                i.getVariantSku(),
                i.getQuantity(),
                i.getUnitPrice(),
                i.getFulfillmentLocation() != null ? i.getFulfillmentLocation().getId() : null,
                i.getFulfillmentLocationName(),
                i.getFulfillmentStatus(),
                i.getBundle() != null ? i.getBundle().getId() : null,
                i.getBundleName(),
                i.getDiscountAmount()
        );
    }

    private CommissionRecordResponse toCommissionResponse(CommissionRecord r) {
        return new CommissionRecordResponse(
                r.getId(),
                r.getSubOrder().getId(),
                r.getVendorId(),
                r.getMarketplaceId(),
                r.getCommissionRate(),
                r.getGrossAmount(),
                r.getCommissionAmount(),
                r.getNetVendorAmount(),
                r.getCurrency(),
                r.getComputedAt()
        );
    }
}
