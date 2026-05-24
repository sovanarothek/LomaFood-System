package com.lomafood.delivery.service;

import com.lomafood.delivery.dto.*;
import com.lomafood.delivery.entity.Delivery;
import com.lomafood.delivery.entity.DeliveryStatus;
import com.lomafood.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String DELIVERY_CACHE_KEY = "delivery:";
    private static final int AVG_SPEED_METERS_PER_MIN = 300;

    @Transactional
    public Delivery createDelivery(CreateDeliveryRequest request) {
        int distanceMeters = estimateDistance(
            request.getPickupLat(), request.getPickupLng(),
            request.getDeliveryLat(), request.getDeliveryLng()
        );
        int durationSeconds = (distanceMeters / AVG_SPEED_METERS_PER_MIN) * 60;

        Delivery delivery = Delivery.builder()
            .orderId(request.getOrderId())
            .restaurantId(request.getRestaurantId())
            .pickupAddress(request.getPickupAddress())
            .deliveryAddress(request.getDeliveryAddress())
            .pickupLat(request.getPickupLat())
            .pickupLng(request.getPickupLng())
            .deliveryLat(request.getDeliveryLat())
            .deliveryLng(request.getDeliveryLng())
            .estimatedDistanceMeters(distanceMeters)
            .estimatedDurationSeconds(durationSeconds)
            .estimatedDeliveryTime(LocalDateTime.now().plusSeconds(durationSeconds))
            .build();

        Delivery saved = deliveryRepository.save(delivery);
        cacheDelivery(saved);
        publishEvent(saved, "DELIVERY_CREATED");
        return saved;
    }

    @Transactional
    public Delivery assignRider(Long deliveryId, Long riderId) {
        Delivery delivery = getDeliveryById(deliveryId);
        delivery.setRiderId(riderId);
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery.setEstimatedPickupTime(LocalDateTime.now().plusMinutes(10));
        Delivery saved = deliveryRepository.save(delivery);
        cacheDelivery(saved);
        publishEvent(saved, "RIDER_ASSIGNED");
        return saved;
    }

    @Transactional
    public Delivery updateStatus(Long deliveryId, UpdateDeliveryStatusRequest request) {
        Delivery delivery = getDeliveryById(deliveryId);
        delivery.setStatus(request.getStatus());

        if (request.getStatus() == DeliveryStatus.DELIVERED) {
            delivery.setActualDeliveryTime(LocalDateTime.now());
        }
        if (request.getFailureReason() != null) {
            delivery.setFailureReason(request.getFailureReason());
        }

        Delivery saved = deliveryRepository.save(delivery);
        cacheDelivery(saved);
        publishEvent(saved, "DELIVERY_STATUS_UPDATED");
        return saved;
    }

    public Delivery getDeliveryByOrderId(Long orderId) {
        String cacheKey = DELIVERY_CACHE_KEY + "order:" + orderId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) return (Delivery) cached;
        return deliveryRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Delivery not found for order: " + orderId));
    }

    public List<Delivery> getRiderDeliveries(Long riderId) {
        return deliveryRepository.findByRiderId(riderId);
    }

    public List<Delivery> getDeliveriesByStatus(DeliveryStatus status) {
        return deliveryRepository.findByStatus(status);
    }

    private Delivery getDeliveryById(Long id) {
        return deliveryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Delivery not found: " + id));
    }

    private void cacheDelivery(Delivery delivery) {
        redisTemplate.opsForValue().set(
            DELIVERY_CACHE_KEY + delivery.getId(), delivery
        );
        redisTemplate.opsForValue().set(
            DELIVERY_CACHE_KEY + "order:" + delivery.getOrderId(), delivery
        );
    }

    private void publishEvent(Delivery delivery, String eventType) {
        DeliveryEventDto event = DeliveryEventDto.builder()
            .deliveryId(delivery.getId())
            .orderId(delivery.getOrderId())
            .riderId(delivery.getRiderId())
            .status(delivery.getStatus())
            .message(eventType)
            .build();
        kafkaTemplate.send("delivery-events", event);
        kafkaTemplate.send("notification-events", event);
        log.info("Published event: {} for delivery: {}", eventType, delivery.getId());
    }

    private int estimateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) return 5000;
        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (int) (earthRadius * c);
    }
}
