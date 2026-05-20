package com.lomafood.restaurant.service;

import com.lomafood.restaurant.dto.*;
import com.lomafood.restaurant.entity.*;
import com.lomafood.restaurant.repository.*;
import com.lomafood.shared.dto.ApiResponse;
import com.lomafood.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepo;
    private final BusinessHoursRepository businessHoursRepo;
    private final MenuItemRepository menuItemRepo;
    private final StringRedisTemplate redisTemplate;

    private static final String RESTAURANT_CACHE = "restaurant:";

    // ---- REGISTER RESTAURANT ----
    public ApiResponse<?> registerRestaurant(String ownerEmail, String ownerId, RestaurantRequest req) {
        Restaurant restaurant = Restaurant.builder()
                .name(req.getName())
                .description(req.getDescription())
                .imageUrl(req.getImageUrl())
                .phone(req.getPhone())
                .email(req.getEmail())
                .street(req.getStreet())
                .city(req.getCity())
                .country(req.getCountry())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .ownerId(UUID.fromString(ownerId))
                .ownerEmail(ownerEmail)
                .deliveryTimeMinutes(req.getDeliveryTimeMinutes())
                .deliveryFee(req.getDeliveryFee())
                .minimumOrder(req.getMinimumOrder())
                .build();
        return ApiResponse.success("Restaurant registered! Pending approval.", restaurantRepo.save(restaurant));
    }

    // ---- GET MY RESTAURANTS ----
    public ApiResponse<?> getMyRestaurants(String ownerEmail) {
        return ApiResponse.success("Restaurants fetched", restaurantRepo.findByOwnerEmail(ownerEmail));
    }

    // ---- GET RESTAURANT BY ID ----
    public ApiResponse<?> getRestaurantById(UUID id) {
        String cacheKey = RESTAURANT_CACHE + id;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return ApiResponse.success("Restaurant fetched from cache", cached);
        }
        Restaurant restaurant = restaurantRepo.findById(id)
                .orElseThrow(() -> new AppException(404, "Restaurant not found"));
        redisTemplate.opsForValue().set(cacheKey, restaurant.toString(), 10, TimeUnit.MINUTES);
        return ApiResponse.success("Restaurant fetched", restaurant);
    }

    // ---- GET ALL OPEN RESTAURANTS ----
    public ApiResponse<?> getOpenRestaurants(String city) {
        List<Restaurant> restaurants = city != null
                ? restaurantRepo.findByCityAndOpenTrue(city)
                : restaurantRepo.findByOpenTrue();
        return ApiResponse.success("Open restaurants fetched", restaurants);
    }

    // ---- UPDATE RESTAURANT ----
    public ApiResponse<?> updateRestaurant(String ownerEmail, UUID id, RestaurantRequest req) {
        Restaurant restaurant = restaurantRepo.findByIdAndOwnerEmail(id, ownerEmail)
                .orElseThrow(() -> new AppException(404, "Restaurant not found"));
        restaurant.setName(req.getName());
        restaurant.setDescription(req.getDescription());
        restaurant.setImageUrl(req.getImageUrl());
        restaurant.setPhone(req.getPhone());
        restaurant.setEmail(req.getEmail());
        restaurant.setStreet(req.getStreet());
        restaurant.setCity(req.getCity());
        restaurant.setCountry(req.getCountry());
        restaurant.setLatitude(req.getLatitude());
        restaurant.setLongitude(req.getLongitude());
        restaurant.setDeliveryTimeMinutes(req.getDeliveryTimeMinutes());
        restaurant.setDeliveryFee(req.getDeliveryFee());
        restaurant.setMinimumOrder(req.getMinimumOrder());
        restaurantRepo.save(restaurant);
        redisTemplate.delete(RESTAURANT_CACHE + id);
        return ApiResponse.success("Restaurant updated", restaurant);
    }

    // ---- TOGGLE OPEN/CLOSE ----
    public ApiResponse<?> toggleOpenStatus(String ownerEmail, UUID id) {
        Restaurant restaurant = restaurantRepo.findByIdAndOwnerEmail(id, ownerEmail)
                .orElseThrow(() -> new AppException(404, "Restaurant not found"));
        restaurant.setOpen(!restaurant.isOpen());
        restaurantRepo.save(restaurant);
        redisTemplate.delete(RESTAURANT_CACHE + id);
        String status = restaurant.isOpen() ? "open" : "closed";
        return ApiResponse.success("Restaurant is now " + status, restaurant);
    }

    // ---- BUSINESS HOURS ----
    @Transactional
    public ApiResponse<?> setBusinessHours(String ownerEmail, UUID restaurantId, BusinessHoursRequest req) {
        Restaurant restaurant = restaurantRepo.findByIdAndOwnerEmail(restaurantId, ownerEmail)
                .orElseThrow(() -> new AppException(404, "Restaurant not found"));
        businessHoursRepo.deleteByRestaurant(restaurant);
        List<BusinessHours> hours = req.getHours().stream().map(h ->
                BusinessHours.builder()
                        .restaurant(restaurant)
                        .dayOfWeek(h.getDayOfWeek())
                        .openTime(h.getOpenTime())
                        .closeTime(h.getCloseTime())
                        .closed(h.isClosed())
                        .build()
        ).toList();
        businessHoursRepo.saveAll(hours);
        return ApiResponse.success("Business hours updated", hours);
    }

    public ApiResponse<?> getBusinessHours(UUID restaurantId) {
        Restaurant restaurant = restaurantRepo.findById(restaurantId)
                .orElseThrow(() -> new AppException(404, "Restaurant not found"));
        return ApiResponse.success("Business hours fetched", businessHoursRepo.findByRestaurant(restaurant));
    }

    // ---- MENU ITEMS ----
    public ApiResponse<?> getMenu(UUID restaurantId) {
        Restaurant restaurant = restaurantRepo.findById(restaurantId)
                .orElseThrow(() -> new AppException(404, "Restaurant not found"));
        return ApiResponse.success("Menu fetched", menuItemRepo.findByRestaurantAndAvailableTrue(restaurant));
    }

    public ApiResponse<?> addMenuItem(String ownerEmail, UUID restaurantId, MenuItemRequest req) {
        Restaurant restaurant = restaurantRepo.findByIdAndOwnerEmail(restaurantId, ownerEmail)
                .orElseThrow(() -> new AppException(404, "Restaurant not found"));
        MenuItem item = MenuItem.builder()
                .restaurant(restaurant)
                .name(req.getName())
                .description(req.getDescription())
                .imageUrl(req.getImageUrl())
                .price(req.getPrice())
                .category(req.getCategory())
                .available(req.isAvailable())
                .popular(req.isPopular())
                .build();
        return ApiResponse.success("Menu item added", menuItemRepo.save(item));
    }

    public ApiResponse<?> updateMenuItem(String ownerEmail, UUID restaurantId, UUID itemId, MenuItemRequest req) {
        restaurantRepo.findByIdAndOwnerEmail(restaurantId, ownerEmail)
                .orElseThrow(() -> new AppException(404, "Restaurant not found"));
        MenuItem item = menuItemRepo.findById(itemId)
                .orElseThrow(() -> new AppException(404, "Menu item not found"));
        item.setName(req.getName());
        item.setDescription(req.getDescription());
        item.setImageUrl(req.getImageUrl());
        item.setPrice(req.getPrice());
        item.setCategory(req.getCategory());
        item.setAvailable(req.isAvailable());
        item.setPopular(req.isPopular());
        return ApiResponse.success("Menu item updated", menuItemRepo.save(item));
    }

    public ApiResponse<?> deleteMenuItem(String ownerEmail, UUID restaurantId, UUID itemId) {
        restaurantRepo.findByIdAndOwnerEmail(restaurantId, ownerEmail)
                .orElseThrow(() -> new AppException(404, "Restaurant not found"));
        menuItemRepo.findById(itemId)
                .orElseThrow(() -> new AppException(404, "Menu item not found"));
        menuItemRepo.deleteById(itemId);
        return ApiResponse.success("Menu item deleted");
    }

    // ---- ADMIN ----
    public ApiResponse<?> approveRestaurant(UUID id) {
        Restaurant restaurant = restaurantRepo.findById(id)
                .orElseThrow(() -> new AppException(404, "Restaurant not found"));
        restaurant.setStatus(RestaurantStatus.APPROVED);
        restaurantRepo.save(restaurant);
        return ApiResponse.success("Restaurant approved");
    }

    public ApiResponse<?> rejectRestaurant(UUID id) {
        Restaurant restaurant = restaurantRepo.findById(id)
                .orElseThrow(() -> new AppException(404, "Restaurant not found"));
        restaurant.setStatus(RestaurantStatus.REJECTED);
        restaurantRepo.save(restaurant);
        return ApiResponse.success("Restaurant rejected");
    }

    public ApiResponse<?> getPendingRestaurants() {
        return ApiResponse.success("Pending restaurants fetched",
                restaurantRepo.findByStatus(RestaurantStatus.PENDING));
    }
}
