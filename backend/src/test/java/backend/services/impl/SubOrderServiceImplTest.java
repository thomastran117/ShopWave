package backend.services.impl;

import backend.dtos.requests.order.CancelSubOrderRequest;
import backend.exceptions.http.ForbiddenException;
import backend.models.core.Company;
import backend.models.core.MarketplaceVendor;
import backend.models.core.Order;
import backend.models.core.OrderItem;
import backend.models.core.SubOrder;
import backend.models.core.User;
import backend.models.enums.FulfillmentStatus;
import backend.models.enums.SubOrderStatus;
import backend.models.enums.UserRole;
import backend.repositories.CommissionRecordRepository;
import backend.repositories.MarketplaceVendorRepository;
import backend.repositories.OrderItemRepository;
import backend.repositories.SubOrderRepository;
import backend.services.impl.orders.SubOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SubOrderServiceImplTest {

    private SubOrderRepository subOrderRepository;
    private OrderItemRepository orderItemRepository;
    private CommissionRecordRepository commissionRecordRepository;
    private MarketplaceVendorRepository marketplaceVendorRepository;
    private SubOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        subOrderRepository = mock(SubOrderRepository.class);
        orderItemRepository = mock(OrderItemRepository.class);
        commissionRecordRepository = mock(CommissionRecordRepository.class);
        marketplaceVendorRepository = mock(MarketplaceVendorRepository.class);
        service = new SubOrderServiceImpl(
                subOrderRepository,
                orderItemRepository,
                commissionRecordRepository,
                marketplaceVendorRepository);
    }

    @Test
    void markPacked_throwsWhenActorDoesNotOwnVendor() {
        MarketplaceVendor vendor = makeVendor(7L, 10L);
        when(marketplaceVendorRepository.findById(7L)).thenReturn(Optional.of(vendor));

        assertThrows(ForbiddenException.class, () -> service.markPacked(100L, 7L, 99L));
    }

    @Test
    void cancelSubOrder_marksPendingPackAndBackorderItemsCancelled() {
        MarketplaceVendor vendor = makeVendor(7L, 10L);
        SubOrder subOrder = makeSubOrder(100L, vendor, SubOrderStatus.PACKED);
        OrderItem pending = makeOrderItem(1L, FulfillmentStatus.PENDING);
        OrderItem packed = makeOrderItem(2L, FulfillmentStatus.PACKED);
        OrderItem backordered = makeOrderItem(3L, FulfillmentStatus.BACKORDERED);

        when(marketplaceVendorRepository.findById(7L)).thenReturn(Optional.of(vendor));
        when(subOrderRepository.findByIdAndMarketplaceVendorId(100L, 7L)).thenReturn(Optional.of(subOrder));
        when(orderItemRepository.findAllBySubOrderId(100L)).thenReturn(List.of(pending, packed, backordered));
        when(subOrderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CancelSubOrderRequest request = new CancelSubOrderRequest();
        request.setReason("Customer requested cancellation");

        service.cancelSubOrder(100L, 7L, request, 10L);

        assertEquals(SubOrderStatus.CANCELLED, subOrder.getStatus());
        assertEquals(FulfillmentStatus.CANCELLED, pending.getFulfillmentStatus());
        assertEquals(FulfillmentStatus.CANCELLED, packed.getFulfillmentStatus());
        assertEquals(FulfillmentStatus.CANCELLED, backordered.getFulfillmentStatus());

        ArgumentCaptor<OrderItem> itemCaptor = ArgumentCaptor.forClass(OrderItem.class);
        verify(orderItemRepository, times(3)).save(itemCaptor.capture());
        itemCaptor.getAllValues().forEach(item ->
                assertEquals(FulfillmentStatus.CANCELLED, item.getFulfillmentStatus()));
    }

    private MarketplaceVendor makeVendor(long vendorId, long ownerId) {
        User owner = new User();
        owner.setId(ownerId);
        owner.setRole(UserRole.VENDOR_OWNER);

        Company company = new Company();
        company.setId(42L);
        company.setName("Vendor Co");
        company.setOwner(owner);

        MarketplaceVendor vendor = new MarketplaceVendor();
        vendor.setId(vendorId);
        vendor.setVendorCompany(company);
        return vendor;
    }

    private SubOrder makeSubOrder(long id, MarketplaceVendor vendor, SubOrderStatus status) {
        Order order = new Order();
        order.setId(500L);

        SubOrder subOrder = new SubOrder();
        subOrder.setId(id);
        subOrder.setOrder(order);
        subOrder.setMarketplaceVendor(vendor);
        subOrder.setMarketplaceId(12L);
        subOrder.setStatus(status);
        subOrder.setSubtotal(BigDecimal.TEN);
        subOrder.setTotalAmount(BigDecimal.TEN);
        subOrder.setCurrency("USD");
        return subOrder;
    }

    private OrderItem makeOrderItem(long id, FulfillmentStatus status) {
        OrderItem item = new OrderItem();
        item.setId(id);
        item.setFulfillmentStatus(status);
        item.setQuantity(1);
        item.setUnitPrice(BigDecimal.ONE);
        item.setDiscountAmount(BigDecimal.ZERO);
        item.setPromotionSavings(BigDecimal.ZERO);
        item.setProductName("Test Item");
        return item;
    }
}
