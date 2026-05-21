package com.lomafood.order.service;

import com.lomafood.order.dto.*;
import com.lomafood.order.entity.*;
import com.lomafood.order.repository.OrderRepository;
import com.lomafood.shared.dto.ApiResponse;
import com.lomafood.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepo;
    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, OrderEventDto> kafkaTemplate;

    private static final String ORDER_CACHE = "order:";
    private static final String TOPIC_ORDER_CREATED = "order.created";
    private static final String TOPIC_ORDER_STATUS = "order.status.updated";
    private static final String TOPIC_ORDER_CANCELLED = "order.cancelled";

    @Transactional
    public ApiResponse<?> createOrder(String userEmail, UUID userId, CreateOrderRequest req) {
        String idempotencyKey = "idempotency:" + userEmail + ":" + req.getRestaurantId();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(idempotencyKey))) {
            throw new AppException(409, "Duplicate order detected. Please wait before ordering again.");
        }

        List<OrderItem> items = req.getItems().stream().map(i -> {
            OrderItem item = new OrderItem();
            item.setMenuItemId(i.getMenuItemId());
            item.setName(i.getName());
            item.setPrice(i.getPrice());
            item.setQuantity(i.getQuantity());
            item.setSubtotal(i.getPrice() * i.getQuantity());
            item.setNotes(i.getNotes());
            return item;
        }).toList();

        double subtotal = items.stream().mapToDouble(OrderItem::getSubtotal).sum();
        double deliveryFee = req.getDeliveryFee() != null ? req.getDeliveryFee() : 0.0;
        double total = subtotal + deliveryFee;

        Order order = Order.builder()
                .userId(userId)
                .userEmail(userEmail)
                .restaurantId(req.getRestaurantId())
                .restaurantName(req.getRestaurantName())
                .subtotal(subtotal)
                .deliveryFee(deliveryFee)
                .total(total)
                .deliveryStreet(req.getDeliveryStreet())
                .deliveryCity(req.getDeliveryCity())
                .deliveryLatitude(req.getDeliveryLatitude())
                .deliveryLongitude(req.getDeliveryLongitude())
                .paymentMethod(req.getPaymentMethod())
                .notes(req.getNotes())
                .build();

        order.setItems(items);
        items.forEach(item -> item.setOrder(order));
        orderRepo.save(order);

        redisTemplate.opsForValue().set(idempotencyKey, "true", 30, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(ORDER_CACHE + order.getId(), order.getStatus().name(), 1, TimeUnit.HOURS);

        OrderEventDto event = new OrderEventDto(
                order.getId(), userId, userEmail,
                req.getRestaurantId(), req.getRestaurantName(),
                OrderStatus.PENDING, total, req.getPaymentMethod()
        );
        kafkaTemplate.send(TOPIC_ORDER_CREATED, order.getId().toString(), event);

        return ApiResponse.success("Order placed successfully", order);
    }

    public ApiResponse<?> getOrderById(UUID orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new AppException(404, "Order not found"));
        return ApiResponse.success("Order fetched", order);
    }

    public ApiResponse<?> getMyOrders(String userEmail) {
        return ApiResponse.success("Orders fetched",
                orderRepo.findByUserEmailOrderByCreatedAtDesc(userEmail));
    }

    public ApiResponse<?> getRestaurantOrders(UUID restaurantId, OrderStatus status) {
        List<Order> orders = status != null
                ? orderRepo.findByRestaurantIdAndStatus(restaurantId, status)
                : orderRepo.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);
        return ApiResponse.success("Restaurant orders fetched", orders);
    }

    public ApiResponse<?> getRiderOrders(UUID riderId) {
        return ApiResponse.success("Rider orders fetched",
                orderRepo.findByRiderIdOrderByCreatedAtDesc(riderId));
    }

    @Transactional
    public ApiResponse<?> updateOrderStatus(UUID orderId, UpdateOrderStatusRequest req) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new AppException(404, "Order not found"));

        validateStatusTransition(order.getStatus(), req.getStatus());

        order.setStatus(req.getStatus());
        if (req.getCancelReason() != null) {
            order.setCancelReason(req.getCancelReason());
        }
        orderRepo.save(order);

        redisTemplate.opsForValue().set(ORDER_CACHE + orderId, req.getStatus().name(), 1, TimeUnit.HOURS);

        OrderEventDto event = new OrderEventDto(
                order.getId(), order.getUserId(), order.getUserEmail(),
                order.getRestaurantId(), order.getRestaurantName(),
                req.getStatus(), order.getTotal(), order.getPaymentMethod()
        );

        String topic = req.getStatus() == OrderStatus.CANCELLED
                ? TOPIC_ORDER_CANCELLED : TOPIC_ORDER_STATUS;
        kafkaTemplate.send(topic, orderId.toString(), event);

        return ApiResponse.success("Order status updated", order);
    }

    @Transactional
    public ApiResponse<?> cancelOrder(String userEmail, UUID orderId, String reason) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new AppException(404, "Order not found"));

        if (!order.getUserEmail().equals(userEmail)) {
            throw new AppException(403, "Not authorized to cancel this order");
        }

        if (order.getStatus() != OrderStatus.PENDING &&
            order.getStatus() != OrderStatus.CONFIRMED) {
            throw new AppException(400, "Order cannot be cancelled at this stage");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason);
        orderRepo.save(order);
        redisTemplate.delete(ORDER_CACHE + orderId);

        OrderEventDto event = new OrderEventDto(
                order.getId(), order.getUserId(), order.getUserEmail(),
                order.getRestaurantId(), order.getRestaurantName(),
                OrderStatus.CANCELLED, order.getTotal(), order.getPaymentMethod()
        );
        kafkaTemplate.send(TOPIC_ORDER_CANCELLED, orderId.toString(), event);

        return ApiResponse.success("Order cancelled successfully");
    }

    @Transactional
    public ApiResponse<?> reorder(String userEmail, UUID userId, UUID orderId) {
        Order originalOrder = orderRepo.findById(orderId)
                .orElseThrow(() -> new AppException(404, "Order not found"));

        if (!originalOrder.getUserEmail().equals(userEmail)) {
            throw new AppException(403, "Not authorized");
        }

        Order newOrder = Order.builder()
                .userId(userId)
                .userEmail(userEmail)
                .restaurantId(originalOrder.getRestaurantId())
                .restaurantName(originalOrder.getRestaurantName())
                .subtotal(originalOrder.getSubtotal())
                .deliveryFee(originalOrder.getDeliveryFee())
                .total(originalOrder.getTotal())
                .deliveryStreet(originalOrder.getDeliveryStreet())
                .deliveryCity(originalOrder.getDeliveryCity())
                .deliveryLatitude(originalOrder.getDeliveryLatitude())
                .deliveryLongitude(originalOrder.getDeliveryLongitude())
                .paymentMethod(originalOrder.getPaymentMethod())
                .notes(originalOrder.getNotes())
                .build();

        List<OrderItem> newItems = originalOrder.getItems().stream().map(i -> {
            OrderItem item = new OrderItem();
            item.setMenuItemId(i.getMenuItemId());
            item.setName(i.getName());
            item.setPrice(i.getPrice());
            item.setQuantity(i.getQuantity());
            item.setSubtotal(i.getSubtotal());
            item.setNotes(i.getNotes());
            item.setOrder(newOrder);
            return item;
        }).toList();

        newOrder.setItems(newItems);
        orderRepo.save(newOrder);

        return ApiResponse.success("Order reordered successfully", newOrder);
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = false;
        if (current == OrderStatus.PENDING) {
            valid = next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
        } else if (current == OrderStatus.CONFIRMED) {
            valid = next == OrderStatus.PREPARING || next == OrderStatus.CANCELLED;
        } else if (current == OrderStatus.PREPARING) {
            valid = next == OrderStatus.READY;
        } else if (current == OrderStatus.READY) {
            valid = next == OrderStatus.DELIVERING;
        } else if (current == OrderStatus.DELIVERING) {
            valid = next == OrderStatus.DELIVERED;
        }
        if (!valid) {
            throw new AppException(400, "Invalid status transition: " + current + " -> " + next);
        }
    }
}
