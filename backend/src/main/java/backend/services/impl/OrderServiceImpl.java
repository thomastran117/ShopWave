package backend.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import backend.dtos.requests.order.CreateOrderRequest;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.order.CompanyOrderResponse;
import backend.dtos.responses.order.OrderItemResponse;
import backend.dtos.responses.order.OrderResponse;
import backend.exceptions.http.BadRequestException;
import backend.exceptions.http.ConflictException;
import backend.exceptions.http.ForbiddenException;
import backend.exceptions.http.ResourceNotFoundException;
import backend.models.core.BundleItem;
import backend.models.core.InventoryAdjustment;
import backend.models.core.LocationStock;
import backend.models.core.Order;
import backend.models.core.OrderCompensation;
import backend.models.core.OrderItem;
import backend.models.core.Product;
import backend.models.core.ProductBundle;
import backend.models.core.ProductVariant;
import backend.models.core.User;
import backend.models.core.Discount;
import backend.models.enums.AdjustmentReason;
import backend.models.enums.CompensationStatus;
import backend.models.enums.CompensationType;
import backend.models.enums.DiscountType;
import backend.models.enums.OrderStatus;
import backend.models.enums.ProductStatus;
import backend.repositories.BundleRepository;
import backend.repositories.DiscountRepository;
import backend.repositories.CompanyRepository;
import backend.repositories.InventoryAdjustmentRepository;
import backend.repositories.InventoryLocationRepository;
import backend.repositories.LocationStockRepository;
import backend.repositories.OrderCompensationRepository;
import backend.repositories.OrderRepository;
import backend.repositories.ProductRepository;
import backend.repositories.ProductVariantRepository;
import backend.repositories.UserRepository;
import backend.services.intf.CacheService;
import backend.services.intf.EmailService;
import backend.services.intf.OrderService;
import backend.services.intf.PaymentService;
import backend.services.intf.PaymentService.PaymentIntentResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private static final String LOCK_PREFIX = "lock:product:";
    private static final String VARIANT_LOCK_PREFIX = "lock:variant:";
    private static final long LOCK_TTL_SECONDS = 10;
    private static final int LOCK_RETRY_ATTEMPTS = 5;
    private static final long LOCK_RETRY_DELAY_MS = 100;
    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "totalAmount");

    private final OrderRepository orderRepository;
    private final OrderCompensationRepository compensationRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final LocationStockRepository locationStockRepository;
    private final InventoryAdjustmentRepository adjustmentRepository;
    private final InventoryLocationRepository locationRepository;
    private final BundleRepository bundleRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final DiscountRepository discountRepository;
    private final PaymentService paymentService;
    private final CacheService cacheService;
    private final StockAlertService stockAlertService;
    private final EmailService emailService;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderCompensationRepository compensationRepository,
            ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            LocationStockRepository locationStockRepository,
            InventoryAdjustmentRepository adjustmentRepository,
            InventoryLocationRepository locationRepository,
            BundleRepository bundleRepository,
            UserRepository userRepository,
            CompanyRepository companyRepository,
            DiscountRepository discountRepository,
            PaymentService paymentService,
            CacheService cacheService,
            StockAlertService stockAlertService,
            EmailService emailService) {
        this.orderRepository = orderRepository;
        this.compensationRepository = compensationRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.locationStockRepository = locationStockRepository;
        this.adjustmentRepository = adjustmentRepository;
        this.locationRepository = locationRepository;
        this.bundleRepository = bundleRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.discountRepository = discountRepository;
        this.paymentService = paymentService;
        this.cacheService = cacheService;
        this.stockAlertService = stockAlertService;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(long userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        List<CreateOrderRequest.OrderItemRequest> itemRequests = request.getItems();

        // Validate: each item must have exactly one of productId or bundleId
        for (CreateOrderRequest.OrderItemRequest ir : itemRequests) {
            if (ir.getProductId() == null && ir.getBundleId() == null) {
                throw new BadRequestException("Each order item must specify either a productId or a bundleId");
            }
            if (ir.getProductId() != null && ir.getBundleId() != null) {
                throw new BadRequestException("Each order item must specify either a productId or a bundleId, not both");
            }
        }

        List<CreateOrderRequest.OrderItemRequest> productItemRequests = itemRequests.stream()
                .filter(i -> i.getBundleId() == null).toList();
        List<CreateOrderRequest.OrderItemRequest> bundleItemRequests = itemRequests.stream()
                .filter(i -> i.getBundleId() != null).toList();

        // Resolve and validate bundles before locking (fail fast)
        Map<Long, ProductBundle> resolvedBundles = new HashMap<>();
        for (CreateOrderRequest.OrderItemRequest ir : bundleItemRequests) {
            long bundleId = ir.getBundleId();
            if (resolvedBundles.containsKey(bundleId)) continue;
            ProductBundle bundle = bundleRepository.findById(bundleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Bundle not found with id: " + bundleId));
            if (bundle.getStatus() != backend.models.enums.ProductStatus.ACTIVE || !bundle.isListed()) {
                throw new BadRequestException("Bundle '" + bundle.getName() + "' is not available for purchase");
            }
            resolvedBundles.put(bundleId, bundle);
        }

        // Collect product IDs from product items
        List<Long> productIds = productItemRequests.stream()
                .map(CreateOrderRequest.OrderItemRequest::getProductId)
                .collect(java.util.stream.Collectors.toCollection(java.util.TreeSet::new))
                .stream().toList();

        // Merge constituent product IDs from all bundles into the lock set (sorted, deduplicated)
        java.util.TreeSet<Long> allProductIdSet = new java.util.TreeSet<>(productIds);
        for (ProductBundle bundle : resolvedBundles.values()) {
            for (BundleItem bi : bundle.getItems()) {
                allProductIdSet.add(bi.getProduct().getId());
            }
        }
        List<Long> allProductIds = new ArrayList<>(allProductIdSet);

        // Reject same productId with different variantIds — ambiguous, can't be safely merged
        Map<Long, Long> seenProductVariant = new HashMap<>();
        for (CreateOrderRequest.OrderItemRequest item : productItemRequests) {
            if (item.getProductId() == null) continue;
            Long existingVariant = seenProductVariant.put(item.getProductId(), item.getVariantId());
            if (existingVariant != null && !Objects.equals(existingVariant, item.getVariantId())) {
                throw new BadRequestException(
                    "Product id " + item.getProductId() + " appears with multiple variants in the same order — submit as separate orders");
            }
        }

        Map<Long, Integer> quantityMap = new HashMap<>();
        Map<Long, Long> variantMap = new HashMap<>();  // productId -> variantId
        for (CreateOrderRequest.OrderItemRequest item : productItemRequests) {
            quantityMap.merge(item.getProductId(), item.getQuantity(), Integer::sum);
            if (item.getVariantId() != null) {
                variantMap.put(item.getProductId(), item.getVariantId());
            }
        }

        // Collect variant IDs that need locks (sorted for deadlock prevention)
        List<Long> variantIdsToLock = variantMap.values().stream().sorted().toList();

        String lockToken = UUID.randomUUID().toString();
        List<String> acquiredLocks = new ArrayList<>();
        List<long[]> decrementedProducts = new ArrayList<>();
        List<long[]> decrementedVariants = new ArrayList<>();
        List<long[]> decrementedLocationStocks = new ArrayList<>();
        // [product, variant (null for product-level), prevStock, newStock]
        record PurchaseRecord(Product prod, ProductVariant var, int prevStock, int newStock) {}
        List<PurchaseRecord> purchaseRecords = new ArrayList<>();

        try {
            acquireLocks(allProductIds, lockToken, acquiredLocks);
            acquireVariantLocks(variantIdsToLock, lockToken, acquiredLocks);

            List<Product> products = productRepository.findAllById(productIds);
            if (products.size() != productIds.size()) {
                throw new ResourceNotFoundException("One or more products not found");
            }

            Map<Long, Product> productMap = new HashMap<>();
            for (Product p : products) {
                productMap.put(p.getId(), p);
            }

            for (Product product : products) {
                if (product.getStatus() != ProductStatus.ACTIVE) {
                    throw new BadRequestException("Product '" + product.getName() + "' is not available for purchase");
                }
                if (!product.isPurchasable()) {
                    throw new BadRequestException("Product '" + product.getName() + "' is not available for purchase");
                }
                boolean hasVariants = variantRepository.existsByProductId(product.getId());
                Long requestedVariantId = variantMap.get(product.getId());
                if (hasVariants && requestedVariantId == null) {
                    throw new BadRequestException("Product '" + product.getName() + "' has variants — specify a variantId");
                }
                if (!hasVariants && requestedVariantId != null) {
                    throw new BadRequestException("Product '" + product.getName() + "' has no variants");
                }
            }

            List<OrderItem> orderItems = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (Long productId : productIds) {
                Product product = productMap.get(productId);
                int qty = quantityMap.get(productId);
                Long variantId = variantMap.get(productId);

                OrderItem item = new OrderItem();
                item.setProduct(product);
                item.setProductName(product.getName());
                item.setQuantity(qty);

                if (variantId != null) {
                    ProductVariant variant = variantRepository.findByIdAndProductId(variantId, productId)
                            .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantId));

                    if (!variant.isPurchasable()) {
                        throw new BadRequestException("Variant is not available for purchase");
                    }

                    int prevVariantStock = variant.getStock() != null ? variant.getStock() : 0;
                    int updated = variantRepository.decrementStock(variantId, qty);
                    if (updated == 0) {
                        if (variant.isBackorderEnabled()) {
                            item.setBackorder(true);
                        } else {
                            safeRestoreAll(decrementedProducts, decrementedVariants, decrementedLocationStocks);
                            throw new ConflictException("Insufficient stock for variant of product '" + product.getName() + "'");
                        }
                    } else {
                        decrementedVariants.add(new long[]{variantId, qty});
                        purchaseRecords.add(new PurchaseRecord(product, variant, prevVariantStock, prevVariantStock - qty));
                    }

                    item.setVariant(variant);
                    item.setVariantTitle(buildVariantTitle(variant));
                    item.setVariantSku(variant.getSku());
                    BigDecimal variantDiscountAmt = computeDiscountAmount(
                            product.getCompany().getId(), product.getId(), variant.getPrice());
                    item.setDiscountAmount(variantDiscountAmt);
                    BigDecimal effectiveVariantPrice = variant.getPrice().subtract(variantDiscountAmt);
                    item.setUnitPrice(effectiveVariantPrice);
                    totalAmount = totalAmount.add(effectiveVariantPrice.multiply(BigDecimal.valueOf(qty)));
                } else {
                    int prevProductStock = product.getStock() != null ? product.getStock() : 0;
                    int updated = productRepository.decrementStock(product.getId(), qty);
                    if (updated == 0) {
                        if (product.isBackorderEnabled()) {
                            item.setBackorder(true);
                        } else {
                            safeRestoreAll(decrementedProducts, decrementedVariants, decrementedLocationStocks);
                            throw new ConflictException("Insufficient stock for product '" + product.getName() + "'");
                        }
                    } else {
                        decrementedProducts.add(new long[]{product.getId(), qty});
                        purchaseRecords.add(new PurchaseRecord(product, null, prevProductStock, prevProductStock - qty));
                    }

                    BigDecimal productDiscountAmt = computeDiscountAmount(
                            product.getCompany().getId(), product.getId(), product.getPrice());
                    item.setDiscountAmount(productDiscountAmt);
                    BigDecimal effectiveProductPrice = product.getPrice().subtract(productDiscountAmt);
                    item.setUnitPrice(effectiveProductPrice);
                    totalAmount = totalAmount.add(effectiveProductPrice.multiply(BigDecimal.valueOf(qty)));
                }

                // Location stock — skip for backorder items (no stock was reserved)
                if (!item.isBackorder()) {
                    long variantRefForLoc = (variantId != null) ? variantId : 0L;
                    List<LocationStock> locCandidates = (variantId != null)
                            ? locationStockRepository.findTopByVariantStockDesc(
                                    productId, variantRefForLoc, org.springframework.data.domain.PageRequest.of(0, 1))
                            : locationStockRepository.findTopByProductStockDesc(
                                    productId, org.springframework.data.domain.PageRequest.of(0, 1));

                    if (!locCandidates.isEmpty()) {
                        LocationStock ls = locCandidates.get(0);
                        int lsUpdated = locationStockRepository.decrementStock(ls.getId(), qty);
                        if (lsUpdated == 0) {
                            safeRestoreAll(decrementedProducts, decrementedVariants, decrementedLocationStocks);
                            throw new ConflictException("Insufficient stock at location '" +
                                    ls.getLocation().getName() + "' for product '" + product.getName() + "'");
                        }
                        decrementedLocationStocks.add(new long[]{ls.getId(), qty});
                        item.setFulfillmentLocation(ls.getLocation());
                        item.setFulfillmentLocationName(ls.getLocation().getName());
                    }
                }

                orderItems.add(item);
            }

            // Process bundle items (inside lock block — all constituent product IDs are already locked)
            for (CreateOrderRequest.OrderItemRequest req : bundleItemRequests) {
                ProductBundle bundle = resolvedBundles.get(req.getBundleId());
                int bundleQty = req.getQuantity();

                OrderItem bundleItem = new OrderItem();
                bundleItem.setBundle(bundle);
                bundleItem.setBundleName(bundle.getName());
                bundleItem.setProduct(null);
                bundleItem.setQuantity(bundleQty);
                bundleItem.setUnitPrice(bundle.getPrice());
                bundleItem.setProductName(bundle.getName());
                totalAmount = totalAmount.add(bundle.getPrice().multiply(BigDecimal.valueOf(bundleQty)));

                for (BundleItem bi : bundle.getItems()) {
                    if (bi.getProduct().getStatus() != ProductStatus.ACTIVE || !bi.getProduct().isPurchasable()) {
                        safeRestoreAll(decrementedProducts, decrementedVariants, decrementedLocationStocks);
                        throw new BadRequestException("Bundle '" + bundle.getName() +
                            "' contains unavailable product '" + bi.getProduct().getName() + "'");
                    }
                    if (bi.getVariant() != null && !bi.getVariant().isPurchasable()) {
                        safeRestoreAll(decrementedProducts, decrementedVariants, decrementedLocationStocks);
                        throw new BadRequestException("Bundle '" + bundle.getName() +
                            "' contains an unavailable variant of '" + bi.getProduct().getName() + "'");
                    }
                    int totalQty = bundleQty * bi.getQuantity();

                    int prevStock, updated;
                    if (bi.getVariant() != null) {
                        prevStock = bi.getVariant().getStock() != null ? bi.getVariant().getStock() : 0;
                        updated = variantRepository.decrementStock(bi.getVariant().getId(), totalQty);
                    } else {
                        prevStock = bi.getProduct().getStock() != null ? bi.getProduct().getStock() : 0;
                        updated = productRepository.decrementStock(bi.getProduct().getId(), totalQty);
                    }

                    if (updated == 0) {
                        safeRestoreAll(decrementedProducts, decrementedVariants, decrementedLocationStocks);
                        throw new ConflictException("Insufficient stock for bundle '" + bundle.getName() +
                                "' (product: '" + bi.getProduct().getName() + "')");
                    }

                    int newStock = prevStock - totalQty;
                    if (bi.getVariant() != null) {
                        decrementedVariants.add(new long[]{bi.getVariant().getId(), totalQty});
                    } else {
                        decrementedProducts.add(new long[]{bi.getProduct().getId(), totalQty});
                    }
                    purchaseRecords.add(new PurchaseRecord(bi.getProduct(), bi.getVariant(), prevStock, newStock));
                }

                orderItems.add(bundleItem);
            }

            String currency = request.getCurrency() != null ? request.getCurrency().toLowerCase() : "usd";
            long amountInCents = totalAmount.multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValueExact();

            Order order = new Order();
            order.setUser(user);
            order.setTotalAmount(totalAmount);
            order.setCurrency(currency);
            order.setStatus(OrderStatus.PENDING);

            for (OrderItem item : orderItems) {
                item.setOrder(order);
            }
            order.setItems(orderItems);

            order = orderRepository.save(order);

            // Record PURCHASE adjustments — order is now persisted so orderId is set.
            // previousStock is captured from the in-memory entity while the lock is held: no race condition.
            List<InventoryAdjustment> purchaseAdjs = new ArrayList<>();
            for (PurchaseRecord pr : purchaseRecords) {
                InventoryAdjustment adj = new InventoryAdjustment();
                adj.setProduct(pr.prod());
                adj.setVariant(pr.var());
                adj.setDelta(pr.newStock() - pr.prevStock()); // negative (e.g. -3)
                adj.setPreviousStock(pr.prevStock());
                adj.setNewStock(pr.newStock());
                adj.setReason(AdjustmentReason.PURCHASE);
                adj.setNote("Order #" + order.getId());
                adj.setOrderId(order.getId());
                purchaseAdjs.add(adj);
            }
            adjustmentRepository.saveAll(purchaseAdjs);

            // Low stock alerts — uses data already captured in purchaseRecords (no extra queries)
            for (PurchaseRecord pr : purchaseRecords) {
                Integer threshold = pr.var() != null
                        ? pr.var().getLowStockThreshold()
                        : pr.prod().getLowStockThreshold();
                stockAlertService.checkAndAlert(
                        pr.prod().getId(), pr.prod().getName(),
                        pr.var() != null ? pr.var().getId() : null,
                        pr.var() != null ? pr.var().getSku() : null,
                        pr.newStock(), threshold);
            }

            PaymentIntentResult paymentIntent;
            try {
                paymentIntent = paymentService.createPaymentIntent(
                        amountInCents,
                        currency,
                        null,
                        Map.of("user_id", String.valueOf(userId), "order_id", String.valueOf(order.getId()))
                );
            } catch (Exception e) {
                order.setStatus(OrderStatus.FAILED);
                order.setFailureReason("Payment intent creation failed: " + e.getMessage());
                orderRepository.save(order);
                scheduleStockCompensation(order, decrementedProducts, decrementedVariants, decrementedLocationStocks);
                throw e;
            }

            order.setPaymentIntentId(paymentIntent.id());
            order.setPaymentClientSecret(paymentIntent.clientSecret());

            OrderResponse response = toResponse(orderRepository.save(order));
            emailService.sendOrderReceiptEmail(user.getEmail(), user.getFirstName(), response);
            return response;
        } catch (ConflictException | ResourceNotFoundException | BadRequestException e) {
            throw e;
        } catch (Exception e) {
            safeRestoreAll(decrementedProducts, decrementedVariants, decrementedLocationStocks);
            throw e;
        } finally {
            releaseLocks(acquiredLocks, lockToken);
        }
    }

    @Override
    public OrderResponse getOrder(long orderId, long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return toResponse(order);
    }

    @Override
    public PagedResponse<OrderResponse> getOrders(long userId, OrderStatus status, int page, int size, String sort, String direction) {
        if (size > 50) size = 50;

        String sortField = (sort != null && SORTABLE_FIELDS.contains(sort)) ? sort : "createdAt";
        Sort.Direction sortDir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortField));

        if (status != null) {
            return new PagedResponse<>(
                    orderRepository.findAllByUserIdAndStatus(userId, status, pageable).map(this::toResponse)
            );
        }
        return new PagedResponse<>(
                orderRepository.findAllByUserId(userId, pageable).map(this::toResponse)
        );
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(long orderId, long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.BACKORDER) {
            throw new ConflictException("Only pending or backorder orders can be cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);

        if (order.getPaymentIntentId() != null) {
            try {
                paymentService.cancelPaymentIntent(order.getPaymentIntentId());
                recordCompensation(order, CompensationType.PAYMENT_CANCEL,
                        "Cancelled payment intent: " + order.getPaymentIntentId(), CompensationStatus.COMPLETED);
            } catch (Exception e) {
                log.error("Failed to cancel payment intent {} for order {}: {}",
                        order.getPaymentIntentId(), order.getId(), e.getMessage());
                recordCompensation(order, CompensationType.PAYMENT_CANCEL,
                        "Failed to cancel payment intent: " + order.getPaymentIntentId(), CompensationStatus.FAILED, e.getMessage());
            }
        }

        for (OrderItem item : order.getItems()) {
            try {
                restoreItemStock(item);
                recordCancelAdjustment(item, order.getId());
                recordCompensation(order, CompensationType.STOCK_RESTORE,
                        buildRestoreDetail(item) + " restored for order cancellation", CompensationStatus.COMPLETED);
            } catch (Exception e) {
                log.error("Failed to restore stock for item on order {}: {}", order.getId(), e.getMessage());
                recordCompensation(order, CompensationType.STOCK_RESTORE,
                        buildRestoreDetail(item) + " failed to restore", CompensationStatus.FAILED, e.getMessage());
            }
        }

        order.setCompensated(true);
        return toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void handlePaymentSuccess(String paymentIntentId) {
        orderRepository.findByPaymentIntentId(paymentIntentId).ifPresent(order -> {
            boolean hasBackorderItems = order.getItems().stream().anyMatch(OrderItem::isBackorder);
            order.setStatus(hasBackorderItems ? OrderStatus.BACKORDER : OrderStatus.PAID);
            orderRepository.save(order);
        });
    }

    @Override
    @Transactional
    public void handlePaymentFailure(String paymentIntentId) {
        orderRepository.findByPaymentIntentId(paymentIntentId).ifPresent(order -> {
            if (order.isCompensated()) return;

            order.setStatus(OrderStatus.FAILED);
            order.setFailureReason("Payment failed via webhook");

            for (OrderItem item : order.getItems()) {
                try {
                    restoreItemStock(item);
                    recordCancelAdjustment(item, order.getId());
                    recordCompensation(order, CompensationType.STOCK_RESTORE,
                            buildRestoreDetail(item) + " restored for payment failure", CompensationStatus.COMPLETED);
                } catch (Exception e) {
                    log.error("Failed to restore stock for item on failed order {}: {}", order.getId(), e.getMessage());
                    recordCompensation(order, CompensationType.STOCK_RESTORE,
                            buildRestoreDetail(item) + " failed to restore", CompensationStatus.FAILED, e.getMessage());
                }
            }

            order.setCompensated(true);
            orderRepository.save(order);
        });
    }

    /**
     * Compensates a single failed/stale order. Called by the scheduler for orders
     * that were not successfully compensated inline. Each step is individually
     * wrapped so a failure in one does not prevent the others from executing.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void compensateOrder(Order order) {
        if (order.isCompensated()) return;

        log.info("Compensating order {} (status={})", order.getId(), order.getStatus());

        for (OrderItem item : order.getItems()) {
            try {
                restoreItemStock(item);
                recordCancelAdjustment(item, order.getId());
                recordCompensation(order, CompensationType.STOCK_RESTORE,
                        buildRestoreDetail(item) + " restored by scheduler", CompensationStatus.COMPLETED);
            } catch (Exception e) {
                log.error("Scheduled compensation: failed to restore stock for item on order {}: {}", order.getId(), e.getMessage());
                recordCompensation(order, CompensationType.STOCK_RESTORE,
                        buildRestoreDetail(item) + " failed in scheduler", CompensationStatus.FAILED, e.getMessage());
            }
        }

        if (order.getPaymentIntentId() != null && order.getStatus() != OrderStatus.CANCELLED) {
            try {
                paymentService.cancelPaymentIntent(order.getPaymentIntentId());
                recordCompensation(order, CompensationType.PAYMENT_CANCEL,
                        "Cancelled payment intent: " + order.getPaymentIntentId(), CompensationStatus.COMPLETED);
            } catch (Exception e) {
                log.error("Scheduled compensation: failed to cancel payment {} for order {}: {}",
                        order.getPaymentIntentId(), order.getId(), e.getMessage());
                recordCompensation(order, CompensationType.PAYMENT_CANCEL,
                        "Cancel payment intent: " + order.getPaymentIntentId(), CompensationStatus.FAILED, e.getMessage());
            }
        }

        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.FAILED);
            order.setFailureReason("Compensated by scheduler — stale pending order");
        }
        order.setCompensated(true);
        orderRepository.save(order);
    }

    /**
     * Retries a single previously failed compensation record. Called by the scheduler
     * to ensure eventual consistency on items that failed their first compensation attempt.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void retryCompensation(OrderCompensation compensation) {
        compensation.setAttempts(compensation.getAttempts() + 1);

        try {
            switch (compensation.getType()) {
                case STOCK_RESTORE -> {
                    String detail = compensation.getDetail();
                    int quantity = extractQuantityFromDetail(detail);
                    if (detail != null && detail.startsWith("[LOC:")) {
                        long locationStockId = extractLocationStockIdFromDetail(detail);
                        if (locationStockId > 0 && quantity > 0) {
                            locationStockRepository.restoreStock(locationStockId, quantity);
                        }
                    } else if (detail != null && detail.startsWith("[VARIANT]")) {
                        long variantId = extractVariantIdFromDetail(detail);
                        if (variantId > 0 && quantity > 0) {
                            variantRepository.restoreStock(variantId, quantity);
                        }
                    } else {
                        long productId = extractProductIdFromDetail(detail);
                        if (productId > 0 && quantity > 0) {
                            productRepository.restoreStock(productId, quantity);
                        }
                    }
                }
                case PAYMENT_CANCEL -> {
                    String intentId = extractIntentIdFromDetail(compensation.getDetail());
                    if (intentId != null) {
                        paymentService.cancelPaymentIntent(intentId);
                    }
                }
                case PAYMENT_REFUND -> {
                    String intentId = extractIntentIdFromDetail(compensation.getDetail());
                    if (intentId != null) {
                        paymentService.refundPayment(intentId, null);
                    }
                }
            }
            compensation.setStatus(CompensationStatus.COMPLETED);
            compensation.setCompletedAt(Instant.now());
            compensation.setErrorMessage(null);
        } catch (Exception e) {
            log.error("Compensation retry failed for id={} type={}: {}",
                    compensation.getId(), compensation.getType(), e.getMessage());
            compensation.setErrorMessage(e.getMessage());
        }

        compensationRepository.save(compensation);
    }

    private void scheduleStockCompensation(Order order, List<long[]> decrementedProducts,
                                            List<long[]> decrementedVariants, List<long[]> decrementedLocationStocks) {
        for (long[] entry : decrementedProducts) {
            try {
                productRepository.restoreStock(entry[0], (int) entry[1]);
                recordCompensation(order, CompensationType.STOCK_RESTORE,
                        "Restored " + entry[1] + " units for product " + entry[0], CompensationStatus.COMPLETED);
            } catch (Exception e) {
                log.error("Inline stock compensation failed for product {} on order {}: {}",
                        entry[0], order.getId(), e.getMessage());
                recordCompensation(order, CompensationType.STOCK_RESTORE,
                        "Restore " + entry[1] + " units for product " + entry[0], CompensationStatus.FAILED, e.getMessage());
            }
        }
        for (long[] entry : decrementedVariants) {
            try {
                variantRepository.restoreStock(entry[0], (int) entry[1]);
                recordCompensation(order, CompensationType.STOCK_RESTORE,
                        "[VARIANT] Restored " + entry[1] + " units for variant " + entry[0], CompensationStatus.COMPLETED);
            } catch (Exception e) {
                log.error("Inline stock compensation failed for variant {} on order {}: {}",
                        entry[0], order.getId(), e.getMessage());
                recordCompensation(order, CompensationType.STOCK_RESTORE,
                        "[VARIANT] Restore " + entry[1] + " units for variant " + entry[0], CompensationStatus.FAILED, e.getMessage());
            }
        }
        for (long[] entry : decrementedLocationStocks) {
            try {
                locationStockRepository.restoreStock(entry[0], (int) entry[1]);
                recordCompensation(order, CompensationType.STOCK_RESTORE,
                        "[LOC:" + entry[0] + "] Restored " + entry[1] + " units", CompensationStatus.COMPLETED);
            } catch (Exception e) {
                log.error("Inline location stock compensation failed for locationStockId {} on order {}: {}",
                        entry[0], order.getId(), e.getMessage());
                recordCompensation(order, CompensationType.STOCK_RESTORE,
                        "[LOC:" + entry[0] + "] Restore " + entry[1] + " units", CompensationStatus.FAILED, e.getMessage());
            }
        }
    }

    /**
     * Returns the largest per-unit saving from any active discount for this product.
     * Returns ZERO if no discount applies. Called while the product lock is held.
     */
    private BigDecimal computeDiscountAmount(long companyId, long productId, BigDecimal basePrice) {
        List<Discount> candidates =
                discountRepository.findActiveDiscountsForProduct(companyId, productId, Instant.now());
        if (candidates.isEmpty()) return BigDecimal.ZERO;
        return candidates.stream()
                .map(d -> effectiveSaving(d, basePrice))
                .max(BigDecimal::compareTo)
                .filter(s -> s.compareTo(BigDecimal.ZERO) > 0)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal effectiveSaving(Discount discount, BigDecimal basePrice) {
        if (discount.getType() == DiscountType.PERCENTAGE) {
            return basePrice.multiply(discount.getValue())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }
        // FIXED_AMOUNT: cap at basePrice so unitPrice is never negative
        return discount.getValue().min(basePrice);
    }

    private void safeRestoreAll(List<long[]> decrementedProducts, List<long[]> decrementedVariants,
                                List<long[]> decrementedLocationStocks) {
        for (long[] entry : decrementedProducts) {
            try {
                productRepository.restoreStock(entry[0], (int) entry[1]);
            } catch (Exception e) {
                log.error("Emergency stock restore failed for product {}: {}", entry[0], e.getMessage());
            }
        }
        for (long[] entry : decrementedVariants) {
            try {
                variantRepository.restoreStock(entry[0], (int) entry[1]);
            } catch (Exception e) {
                log.error("Emergency stock restore failed for variant {}: {}", entry[0], e.getMessage());
            }
        }
        for (long[] entry : decrementedLocationStocks) {
            try {
                locationStockRepository.restoreStock(entry[0], (int) entry[1]);
            } catch (Exception e) {
                log.error("Emergency location stock restore failed for locationStockId {}: {}", entry[0], e.getMessage());
            }
        }
    }

    private void restoreItemStock(OrderItem item) {
        if (item.isBackorder()) return;  // no stock was decremented for backorder items — nothing to restore

        if (item.getBundle() != null) {
            // Restore each constituent product's stock (no location stock for bundle items)
            for (BundleItem bi : item.getBundle().getItems()) {
                int totalQty = item.getQuantity() * bi.getQuantity();
                if (bi.getVariant() != null) {
                    variantRepository.restoreStock(bi.getVariant().getId(), totalQty);
                } else {
                    productRepository.restoreStock(bi.getProduct().getId(), totalQty);
                }
            }
            return;
        }

        if (item.getVariant() != null) {
            variantRepository.restoreStock(item.getVariant().getId(), item.getQuantity());
        } else {
            productRepository.restoreStock(item.getProduct().getId(), item.getQuantity());
        }
        if (item.getFulfillmentLocation() != null) {
            long variantRef = item.getVariant() != null ? item.getVariant().getId() : 0L;
            locationStockRepository.findByLocationIdAndProductIdAndVariantRef(
                            item.getFulfillmentLocation().getId(), item.getProduct().getId(), variantRef)
                    .ifPresent(ls -> locationStockRepository.restoreStock(ls.getId(), item.getQuantity()));
        }
    }

    private void recordCompensation(Order order, CompensationType type, String detail, CompensationStatus status) {
        recordCompensation(order, type, detail, status, null);
    }

    private void recordCompensation(Order order, CompensationType type, String detail, CompensationStatus status, String errorMessage) {
        OrderCompensation comp = new OrderCompensation();
        comp.setOrder(order);
        comp.setType(type);
        comp.setDetail(detail);
        comp.setStatus(status);
        comp.setErrorMessage(errorMessage);
        comp.setAttempts(1);
        if (status == CompensationStatus.COMPLETED) {
            comp.setCompletedAt(Instant.now());
        }
        compensationRepository.save(comp);
    }

    /**
     * Records an ORDER_CANCELLED adjustment for a single order item after stock has been restored.
     * previousStock/newStock are set to 0 — the restore is atomic and reading before it would be a
     * TOCTOU race. The delta (+qty) and orderId are the authoritative audit values.
     */
    private void recordCancelAdjustment(OrderItem item, long orderId) {
        try {
            if (item.getBundle() != null) {
                // One adjustment per bundle constituent
                for (BundleItem bi : item.getBundle().getItems()) {
                    int totalQty = item.getQuantity() * bi.getQuantity();
                    InventoryAdjustment adj = new InventoryAdjustment();
                    adj.setProduct(bi.getProduct());
                    adj.setVariant(bi.getVariant());
                    adj.setDelta(totalQty);
                    adj.setPreviousStock(0);
                    adj.setNewStock(0);
                    adj.setReason(AdjustmentReason.ORDER_CANCELLED);
                    adj.setNote("Bundle order #" + orderId + " cancelled — bundle: " + item.getBundleName());
                    adj.setOrderId(orderId);
                    adjustmentRepository.save(adj);
                }
                return;
            }

            InventoryAdjustment adj = new InventoryAdjustment();
            adj.setProduct(item.getProduct());
            adj.setVariant(item.getVariant());
            adj.setDelta(item.getQuantity()); // positive = stock returned
            adj.setPreviousStock(0);
            adj.setNewStock(0);
            adj.setReason(AdjustmentReason.ORDER_CANCELLED);
            adj.setNote("Order #" + orderId + " cancelled/failed");
            adj.setOrderId(orderId);
            adjustmentRepository.save(adj);
        } catch (Exception e) {
            log.warn("Failed to record cancel adjustment for order {}: {}", orderId, e.getMessage());
        }
    }

    private static String buildRestoreDetail(OrderItem item) {
        if (item.getBundle() != null) {
            return "[BUNDLE:" + item.getBundle().getId() + "] " + item.getQuantity() + " unit(s) of bundle " + item.getBundleName();
        }
        if (item.getVariant() != null) {
            return "[VARIANT] " + item.getQuantity() + " units for variant " + item.getVariant().getId();
        }
        return "Restored " + item.getQuantity() + " units for product " +
                (item.getProduct() != null ? item.getProduct().getId() : "unknown");
    }

    private void acquireVariantLocks(List<Long> sortedVariantIds, String lockToken, List<String> acquiredLocks) {
        for (Long variantId : sortedVariantIds) {
            String lockKey = VARIANT_LOCK_PREFIX + variantId;
            boolean acquired = false;

            for (int attempt = 0; attempt < LOCK_RETRY_ATTEMPTS; attempt++) {
                if (cacheService.tryLock(lockKey, lockToken, LOCK_TTL_SECONDS)) {
                    acquiredLocks.add(lockKey);
                    acquired = true;
                    break;
                }
                try {
                    Thread.sleep(LOCK_RETRY_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new ConflictException("Order processing interrupted, please try again");
                }
            }

            if (!acquired) {
                releaseLocks(acquiredLocks, lockToken);
                throw new ConflictException("Variant is currently being purchased by another user, please try again shortly");
            }
        }
    }

    private void acquireLocks(List<Long> sortedProductIds, String lockToken, List<String> acquiredLocks) {
        for (Long productId : sortedProductIds) {
            String lockKey = LOCK_PREFIX + productId;
            boolean acquired = false;

            for (int attempt = 0; attempt < LOCK_RETRY_ATTEMPTS; attempt++) {
                if (cacheService.tryLock(lockKey, lockToken, LOCK_TTL_SECONDS)) {
                    acquiredLocks.add(lockKey);
                    acquired = true;
                    break;
                }
                try {
                    Thread.sleep(LOCK_RETRY_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new ConflictException("Order processing interrupted, please try again");
                }
            }

            if (!acquired) {
                releaseLocks(acquiredLocks, lockToken);
                throw new ConflictException("Product is currently being purchased by another user, please try again shortly");
            }
        }
    }

    private void releaseLocks(List<String> lockKeys, String lockToken) {
        for (String lockKey : lockKeys) {
            try {
                cacheService.unlock(lockKey, lockToken);
            } catch (Exception e) {
                log.error("Failed to release lock {}: {}", lockKey, e.getMessage());
            }
        }
    }

    private static long extractProductIdFromDetail(String detail) {
        try {
            int idx = detail.lastIndexOf("product ");
            if (idx >= 0) return Long.parseLong(detail.substring(idx + 8).trim());
        } catch (Exception ignored) {}
        return -1;
    }

    private static String buildVariantTitle(ProductVariant variant) {
        String title = java.util.stream.Stream.of(variant.getOption1(), variant.getOption2(), variant.getOption3())
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.joining(" / "));
        return title.isBlank() ? null : title;
    }

    private static long extractVariantIdFromDetail(String detail) {
        try {
            int idx = detail.lastIndexOf("variant ");
            if (idx >= 0) return Long.parseLong(detail.substring(idx + 8).trim());
        } catch (Exception ignored) {}
        return -1;
    }

    // Parses "[LOC:42] Restored 3 units" → 42
    private static long extractLocationStockIdFromDetail(String detail) {
        try {
            int start = detail.indexOf("[LOC:") + 5;
            int end = detail.indexOf("]", start);
            if (start > 4 && end > start) return Long.parseLong(detail.substring(start, end).trim());
        } catch (Exception ignored) {}
        return -1;
    }

    private static int extractQuantityFromDetail(String detail) {
        try {
            int startIdx = detail.indexOf("Restore ") >= 0 ? detail.indexOf("Restore ") + 8 : detail.indexOf("Restored ") + 9;
            int endIdx = detail.indexOf(" units");
            if (startIdx > 0 && endIdx > startIdx) return Integer.parseInt(detail.substring(startIdx, endIdx).trim());
        } catch (Exception ignored) {}
        return -1;
    }

    private static String extractIntentIdFromDetail(String detail) {
        if (detail == null) return null;
        int idx = detail.lastIndexOf(": ");
        if (idx >= 0) return detail.substring(idx + 2).trim();
        idx = detail.lastIndexOf("intent: ");
        if (idx >= 0) return detail.substring(idx + 8).trim();
        return null;
    }

    @Override
    @Transactional
    public void fulfillPendingBackorders(long productId, Long variantId, int availableQty, Long fulfillmentLocationId) {
        List<Order> backorders = (variantId != null)
                ? orderRepository.findBackordersByVariantId(variantId)
                : orderRepository.findBackordersByProductId(productId);

        int remaining = availableQty;

        for (Order order : backorders) {
            if (remaining <= 0) break;

            for (OrderItem item : order.getItems()) {
                if (!item.isBackorder()) continue;
                if (item.getProduct().getId() != productId) continue;
                if (variantId != null && (item.getVariant() == null || item.getVariant().getId() != variantId)) continue;

                int qty = item.getQuantity();
                if (qty > remaining) return; // FIFO: stop rather than skip to a younger order

                int updated = (variantId != null)
                        ? variantRepository.decrementStock(variantId, qty)
                        : productRepository.decrementStock(productId, qty);

                if (updated == 0) {
                    log.warn("fulfillPendingBackorders: decrementStock returned 0 for product={} variant={} qty={} — stopping",
                            productId, variantId, qty);
                    return;
                }

                remaining -= qty;
                item.setBackorder(false);

                if (fulfillmentLocationId != null) {
                    locationRepository.findById(fulfillmentLocationId).ifPresent(loc -> {
                        item.setFulfillmentLocation(loc);
                        item.setFulfillmentLocationName(loc.getName());
                    });
                }

                InventoryAdjustment adj = new InventoryAdjustment();
                adj.setProduct(item.getProduct());
                adj.setVariant(item.getVariant());
                adj.setDelta(-qty); // stock was decremented to fulfill this item
                adj.setPreviousStock(0); // TOCTOU-safe: delta is authoritative
                adj.setNewStock(0);
                adj.setReason(AdjustmentReason.BACKORDER_FULFILLED);
                adj.setNote("Backorder fulfilled for order #" + order.getId());
                adj.setOrderId(order.getId());
                adjustmentRepository.save(adj);
            }

            boolean allFulfilled = order.getItems().stream().noneMatch(OrderItem::isBackorder);
            if (allFulfilled) {
                order.setStatus(OrderStatus.PAID);
            }
            orderRepository.save(order);
        }
    }

    @Override
    public PagedResponse<CompanyOrderResponse> getCompanyOrders(long companyId, long ownerId, OrderStatus status, int page, int size) {
        companyRepository.findByIdAndOwnerId(companyId, ownerId)
                .orElseThrow(() -> new ForbiddenException("You do not own this company"));

        if (size > 50) size = 50;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (status != null) {
            return new PagedResponse<>(
                    orderRepository.findAllByProductCompanyIdAndStatus(companyId, status, pageable)
                            .map(o -> toCompanyOrderResponse(o, companyId)));
        }
        return new PagedResponse<>(
                orderRepository.findAllByProductCompanyId(companyId, pageable)
                        .map(o -> toCompanyOrderResponse(o, companyId)));
    }

    @Override
    public CompanyOrderResponse getCompanyOrder(long companyId, long orderId, long ownerId) {
        companyRepository.findByIdAndOwnerId(companyId, ownerId)
                .orElseThrow(() -> new ForbiddenException("You do not own this company"));

        Order order = orderRepository.findByIdAndProductCompanyId(orderId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        return toCompanyOrderResponse(order, companyId);
    }

    private CompanyOrderResponse toCompanyOrderResponse(Order order, long companyId) {
        List<OrderItem> companyItems = order.getItems().stream()
                .filter(item -> item.getBundle() != null
                        ? item.getBundle().getCompany().getId() == companyId
                        : item.getProduct() != null && item.getProduct().getCompany().getId() == companyId)
                .toList();

        BigDecimal total = companyItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<OrderItemResponse> itemResponses = companyItems.stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProduct() != null ? item.getProduct().getId() : null,
                        item.getProductName(),
                        item.getVariant() != null ? item.getVariant().getId() : null,
                        item.getVariantTitle(),
                        item.getVariantSku(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getFulfillmentLocation() != null ? item.getFulfillmentLocation().getId() : null,
                        item.getFulfillmentLocationName(),
                        item.isBackorder(),
                        item.getBundle() != null ? item.getBundle().getId() : null,
                        item.getBundleName(),
                        item.getDiscountAmount()))
                .toList();

        return new CompanyOrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getStatus().name(),
                order.getCurrency(),
                total,
                itemResponses,
                order.getCreatedAt());
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProduct() != null ? item.getProduct().getId() : null,
                        item.getProductName(),
                        item.getVariant() != null ? item.getVariant().getId() : null,
                        item.getVariantTitle(),
                        item.getVariantSku(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getFulfillmentLocation() != null ? item.getFulfillmentLocation().getId() : null,
                        item.getFulfillmentLocationName(),
                        item.isBackorder(),
                        item.getBundle() != null ? item.getBundle().getId() : null,
                        item.getBundleName(),
                        item.getDiscountAmount()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                items,
                order.getTotalAmount(),
                order.getCurrency(),
                order.getStatus().name(),
                order.getPaymentIntentId(),
                order.getPaymentClientSecret(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
