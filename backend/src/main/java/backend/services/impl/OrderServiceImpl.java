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
import backend.models.core.LocationStock;
import backend.models.core.Order;
import backend.models.core.OrderCompensation;
import backend.models.core.OrderItem;
import backend.models.core.Product;
import backend.models.core.ProductVariant;
import backend.models.core.User;
import backend.models.enums.CompensationStatus;
import backend.models.enums.CompensationType;
import backend.models.enums.OrderStatus;
import backend.models.enums.ProductStatus;
import backend.repositories.CompanyRepository;
import backend.repositories.LocationStockRepository;
import backend.repositories.OrderCompensationRepository;
import backend.repositories.OrderRepository;
import backend.repositories.ProductRepository;
import backend.repositories.ProductVariantRepository;
import backend.repositories.UserRepository;
import backend.services.intf.CacheService;
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
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PaymentService paymentService;
    private final CacheService cacheService;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderCompensationRepository compensationRepository,
            ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            LocationStockRepository locationStockRepository,
            UserRepository userRepository,
            CompanyRepository companyRepository,
            PaymentService paymentService,
            CacheService cacheService) {
        this.orderRepository = orderRepository;
        this.compensationRepository = compensationRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.locationStockRepository = locationStockRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.paymentService = paymentService;
        this.cacheService = cacheService;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(long userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        List<CreateOrderRequest.OrderItemRequest> itemRequests = request.getItems();
        List<Long> productIds = itemRequests.stream()
                .map(CreateOrderRequest.OrderItemRequest::getProductId)
                .distinct()
                .sorted()
                .toList();

        Map<Long, Integer> quantityMap = new HashMap<>();
        Map<Long, Long> variantMap = new HashMap<>();  // productId -> variantId
        for (CreateOrderRequest.OrderItemRequest item : itemRequests) {
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

        try {
            acquireLocks(productIds, lockToken, acquiredLocks);
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

                    int updated = variantRepository.decrementStock(variantId, qty);
                    if (updated == 0) {
                        safeRestoreAll(decrementedProducts, decrementedVariants, decrementedLocationStocks);
                        throw new ConflictException("Insufficient stock for variant of product '" + product.getName() + "'");
                    }
                    decrementedVariants.add(new long[]{variantId, qty});

                    item.setVariant(variant);
                    item.setVariantTitle(buildVariantTitle(variant));
                    item.setVariantSku(variant.getSku());
                    item.setUnitPrice(variant.getPrice());
                    totalAmount = totalAmount.add(variant.getPrice().multiply(BigDecimal.valueOf(qty)));
                } else {
                    int updated = productRepository.decrementStock(product.getId(), qty);
                    if (updated == 0) {
                        safeRestoreAll(decrementedProducts, decrementedVariants, decrementedLocationStocks);
                        throw new ConflictException("Insufficient stock for product '" + product.getName() + "'");
                    }
                    decrementedProducts.add(new long[]{product.getId(), qty});

                    item.setUnitPrice(product.getPrice());
                    totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(qty)));
                }

                // Location stock — pick best-stocked active location (if any exist for this product)
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

                orderItems.add(item);
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

            return toResponse(orderRepository.save(order));
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

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ConflictException("Only pending orders can be cancelled");
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
                if (item.getVariant() != null) {
                    recordCompensation(order, CompensationType.STOCK_RESTORE,
                            "[VARIANT] Restored " + item.getQuantity() + " units for variant " + item.getVariant().getId(), CompensationStatus.COMPLETED);
                } else {
                    recordCompensation(order, CompensationType.STOCK_RESTORE,
                            "Restored " + item.getQuantity() + " units for product " + item.getProduct().getId(), CompensationStatus.COMPLETED);
                }
            } catch (Exception e) {
                log.error("Failed to restore stock for item on order {}: {}", order.getId(), e.getMessage());
                if (item.getVariant() != null) {
                    recordCompensation(order, CompensationType.STOCK_RESTORE,
                            "[VARIANT] Failed to restore " + item.getQuantity() + " units for variant " + item.getVariant().getId(), CompensationStatus.FAILED, e.getMessage());
                } else {
                    recordCompensation(order, CompensationType.STOCK_RESTORE,
                            "Failed to restore " + item.getQuantity() + " units for product " + item.getProduct().getId(), CompensationStatus.FAILED, e.getMessage());
                }
            }
        }

        order.setCompensated(true);
        return toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void handlePaymentSuccess(String paymentIntentId) {
        orderRepository.findByPaymentIntentId(paymentIntentId).ifPresent(order -> {
            order.setStatus(OrderStatus.PAID);
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
                    if (item.getVariant() != null) {
                        recordCompensation(order, CompensationType.STOCK_RESTORE,
                                "[VARIANT] Restored " + item.getQuantity() + " units for variant " + item.getVariant().getId(), CompensationStatus.COMPLETED);
                    } else {
                        recordCompensation(order, CompensationType.STOCK_RESTORE,
                                "Restored " + item.getQuantity() + " units for product " + item.getProduct().getId(), CompensationStatus.COMPLETED);
                    }
                } catch (Exception e) {
                    log.error("Failed to restore stock for item on failed order {}: {}", order.getId(), e.getMessage());
                    if (item.getVariant() != null) {
                        recordCompensation(order, CompensationType.STOCK_RESTORE,
                                "[VARIANT] Failed to restore " + item.getQuantity() + " units for variant " + item.getVariant().getId(), CompensationStatus.FAILED, e.getMessage());
                    } else {
                        recordCompensation(order, CompensationType.STOCK_RESTORE,
                                "Failed to restore " + item.getQuantity() + " units for product " + item.getProduct().getId(), CompensationStatus.FAILED, e.getMessage());
                    }
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
                if (item.getVariant() != null) {
                    recordCompensation(order, CompensationType.STOCK_RESTORE,
                            "[VARIANT] Restored " + item.getQuantity() + " units for variant " + item.getVariant().getId(), CompensationStatus.COMPLETED);
                } else {
                    recordCompensation(order, CompensationType.STOCK_RESTORE,
                            "Restored " + item.getQuantity() + " units for product " + item.getProduct().getId(), CompensationStatus.COMPLETED);
                }
            } catch (Exception e) {
                log.error("Scheduled compensation: failed to restore stock for item on order {}: {}", order.getId(), e.getMessage());
                if (item.getVariant() != null) {
                    recordCompensation(order, CompensationType.STOCK_RESTORE,
                            "[VARIANT] Restore " + item.getQuantity() + " units for variant " + item.getVariant().getId(), CompensationStatus.FAILED, e.getMessage());
                } else {
                    recordCompensation(order, CompensationType.STOCK_RESTORE,
                            "Restore " + item.getQuantity() + " units for product " + item.getProduct().getId(), CompensationStatus.FAILED, e.getMessage());
                }
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
                .filter(item -> item.getProduct().getCompany().getId() == companyId)
                .toList();

        BigDecimal total = companyItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<OrderItemResponse> itemResponses = companyItems.stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProductName(),
                        item.getVariant() != null ? item.getVariant().getId() : null,
                        item.getVariantTitle(),
                        item.getVariantSku(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getFulfillmentLocation() != null ? item.getFulfillmentLocation().getId() : null,
                        item.getFulfillmentLocationName()))
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
                        item.getProduct().getId(),
                        item.getProductName(),
                        item.getVariant() != null ? item.getVariant().getId() : null,
                        item.getVariantTitle(),
                        item.getVariantSku(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getFulfillmentLocation() != null ? item.getFulfillmentLocation().getId() : null,
                        item.getFulfillmentLocationName()
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
