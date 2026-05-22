package com.lomafood.rider.service;

import com.lomafood.rider.dto.*;
import com.lomafood.rider.entity.*;
import com.lomafood.rider.repository.*;
import com.lomafood.shared.dto.ApiResponse;
import com.lomafood.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiderService {

    private final RiderRepository riderRepo;
    private final RiderRatingRepository ratingRepo;
    private final EarningRepository earningRepo;
    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, RiderEventDto> kafkaTemplate;

    private static final String RIDER_LOCATION = "rider:location:";
    private static final String RIDER_STATUS = "rider:status:";
    private static final String TOPIC_RIDER_LOCATION = "rider.location.updated";
    private static final String TOPIC_RIDER_ASSIGNED = "rider.assigned";

    // ---- REGISTER RIDER ----
    public ApiResponse<?> registerRider(String email, UUID userId, RegisterRiderRequest req) {
        if (riderRepo.findByEmail(email).isPresent()) {
            throw new AppException(400, "Rider already registered");
        }
        Rider rider = Rider.builder()
                .id(userId)
                .email(email)
                .name(req.getName())
                .phone(req.getPhone())
                .vehicleType(req.getVehicleType())
                .vehiclePlate(req.getVehiclePlate())
                .build();
        riderRepo.save(rider);
        return ApiResponse.success("Rider registered! Pending approval.", rider);
    }

    // ---- GET PROFILE ----
    public ApiResponse<?> getProfile(String email) {
        Rider rider = getRiderByEmail(email);
        return ApiResponse.success("Rider profile fetched", rider);
    }

    // ---- TOGGLE AVAILABILITY ----
    public ApiResponse<?> toggleAvailability(String email) {
        Rider rider = getRiderByEmail(email);

        if (rider.getStatus() != RiderStatus.APPROVED) {
            throw new AppException(403, "Rider not approved yet");
        }

        RiderAvailability newAvailability = rider.getAvailability() == RiderAvailability.ONLINE
                ? RiderAvailability.OFFLINE : RiderAvailability.ONLINE;

        rider.setAvailability(newAvailability);
        riderRepo.save(rider);

        // Update Redis
        redisTemplate.opsForValue().set(
            RIDER_STATUS + rider.getId(),
            newAvailability.name(),
            24, TimeUnit.HOURS
        );

        return ApiResponse.success("Rider is now " + newAvailability.name().toLowerCase(), rider);
    }

    // ---- UPDATE LOCATION ----
    public ApiResponse<?> updateLocation(String email, UpdateLocationRequest req) {
        Rider rider = getRiderByEmail(email);

        rider.setLatitude(req.getLatitude());
        rider.setLongitude(req.getLongitude());
        rider.setLocationUpdatedAt(LocalDateTime.now());
        riderRepo.save(rider);

        // Cache location in Redis
        String locationData = req.getLatitude() + "," + req.getLongitude();
        redisTemplate.opsForValue().set(
            RIDER_LOCATION + rider.getId(),
            locationData,
            10, TimeUnit.MINUTES
        );

        // Publish location event
        RiderEventDto event = new RiderEventDto(
                rider.getId(), email, null,
                rider.getAvailability(),
                req.getLatitude(), req.getLongitude()
        );
        kafkaTemplate.send(TOPIC_RIDER_LOCATION, rider.getId().toString(), event);

        return ApiResponse.success("Location updated");
    }

    // ---- ACCEPT ORDER ----
    @Transactional
    public ApiResponse<?> acceptOrder(String email, UUID orderId) {
        Rider rider = getRiderByEmail(email);

        if (rider.getAvailability() != RiderAvailability.ONLINE) {
            throw new AppException(400, "Rider must be online to accept orders");
        }

        rider.setAvailability(RiderAvailability.BUSY);
        riderRepo.save(rider);

        redisTemplate.opsForValue().set(
            RIDER_STATUS + rider.getId(),
            RiderAvailability.BUSY.name(),
            24, TimeUnit.HOURS
        );

        RiderEventDto event = new RiderEventDto(
                rider.getId(), email, orderId,
                RiderAvailability.BUSY,
                rider.getLatitude(), rider.getLongitude()
        );
        kafkaTemplate.send(TOPIC_RIDER_ASSIGNED, rider.getId().toString(), event);

        return ApiResponse.success("Order accepted", event);
    }

    // ---- COMPLETE DELIVERY ----
    @Transactional
    public ApiResponse<?> completeDelivery(String email, UUID orderId, Double earnings) {
        Rider rider = getRiderByEmail(email);

        rider.setAvailability(RiderAvailability.ONLINE);
        rider.setTotalDeliveries(rider.getTotalDeliveries() + 1);
        rider.setTotalEarnings(rider.getTotalEarnings() + earnings);
        riderRepo.save(rider);

        // Save earning record
        Earning earning = Earning.builder()
                .riderId(rider.getId())
                .orderId(orderId)
                .amount(earnings)
                .description("Delivery earning for order " + orderId)
                .build();
        earningRepo.save(earning);

        redisTemplate.opsForValue().set(
            RIDER_STATUS + rider.getId(),
            RiderAvailability.ONLINE.name(),
            24, TimeUnit.HOURS
        );

        return ApiResponse.success("Delivery completed", earning);
    }

    // ---- GET EARNINGS ----
    public ApiResponse<?> getEarnings(String email) {
        Rider rider = getRiderByEmail(email);
        return ApiResponse.success("Earnings fetched", earningRepo.findByRiderId(rider.getId()));
    }

    // ---- GET UNPAID EARNINGS ----
    public ApiResponse<?> getUnpaidEarnings(String email) {
        Rider rider = getRiderByEmail(email);
        Double unpaid = earningRepo.getTotalUnpaidEarnings(rider.getId());
        return ApiResponse.success("Unpaid earnings", unpaid != null ? unpaid : 0.0);
    }

    // ---- RATE RIDER ----
    @Transactional
    public ApiResponse<?> rateRider(String userEmail, UUID riderId, RateRiderRequest req) {
        Rider rider = riderRepo.findById(riderId)
                .orElseThrow(() -> new AppException(404, "Rider not found"));

        RiderRating rating = RiderRating.builder()
                .riderId(riderId)
                .orderId(req.getOrderId())
                .userId(null)
                .userEmail(userEmail)
                .rating(req.getRating())
                .comment(req.getComment())
                .build();
        ratingRepo.save(rating);

        // Update average rating
        Double avgRating = ratingRepo.getAverageRating(riderId);
        rider.setRating(avgRating != null ? avgRating : 0.0);
        rider.setTotalRatings(rider.getTotalRatings() + 1);
        riderRepo.save(rider);

        return ApiResponse.success("Rider rated successfully", rating);
    }

    // ---- GET RATINGS ----
    public ApiResponse<?> getRatings(UUID riderId) {
        return ApiResponse.success("Ratings fetched", ratingRepo.findByRiderId(riderId));
    }

    // ---- GET AVAILABLE RIDERS ----
    public ApiResponse<?> getAvailableRiders() {
        return ApiResponse.success("Available riders fetched",
                riderRepo.findByStatusAndAvailability(
                        RiderStatus.APPROVED, RiderAvailability.ONLINE));
    }

    // ---- ADMIN ----
    public ApiResponse<?> approveRider(UUID riderId) {
        Rider rider = riderRepo.findById(riderId)
                .orElseThrow(() -> new AppException(404, "Rider not found"));
        rider.setStatus(RiderStatus.APPROVED);
        rider.setVerified(true);
        riderRepo.save(rider);
        return ApiResponse.success("Rider approved");
    }

    public ApiResponse<?> rejectRider(UUID riderId) {
        Rider rider = riderRepo.findById(riderId)
                .orElseThrow(() -> new AppException(404, "Rider not found"));
        rider.setStatus(RiderStatus.REJECTED);
        riderRepo.save(rider);
        return ApiResponse.success("Rider rejected");
    }

    public ApiResponse<?> getPendingRiders() {
        return ApiResponse.success("Pending riders fetched",
                riderRepo.findByStatus(RiderStatus.PENDING));
    }

    // ---- HELPER ----
    private Rider getRiderByEmail(String email) {
        return riderRepo.findByEmail(email)
                .orElseThrow(() -> new AppException(404, "Rider not found"));
    }
}
