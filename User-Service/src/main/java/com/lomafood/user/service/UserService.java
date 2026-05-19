package com.lomafood.user.service;

import com.lomafood.user.dto.*;
import com.lomafood.user.entity.*;
import com.lomafood.user.repository.*;
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
public class UserService {

    private final UserProfileRepository userRepo;
    private final AddressRepository addressRepo;
    private final FavoriteRepository favoriteRepo;
    private final PaymentMethodRepository paymentRepo;
    private final StringRedisTemplate redisTemplate;

    private static final String USER_CACHE = "user:";

    // ---- GET PROFILE ----
    public ApiResponse<?> getProfile(String email) {
        // Check Redis cache first
        String cacheKey = USER_CACHE + email;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return ApiResponse.success("Profile fetched from cache", cached);
        }

        UserProfile user = userRepo.findByEmail(email)
                .orElseThrow(() -> new AppException(404, "User not found"));

        UserProfileResponse response = mapToResponse(user);

        // Cache for 10 minutes
        redisTemplate.opsForValue().set(cacheKey, response.toString(), 10, TimeUnit.MINUTES);

        return ApiResponse.success("Profile fetched", response);
    }

    // ---- UPDATE PROFILE ----
    public ApiResponse<?> updateProfile(String email, UpdateProfileRequest req) {
        UserProfile user = userRepo.findByEmail(email)
                .orElseThrow(() -> new AppException(404, "User not found"));

        user.setName(req.getName());
        user.setPhone(req.getPhone());
        user.setBio(req.getBio());
        userRepo.save(user);

        // Invalidate cache
        redisTemplate.delete(USER_CACHE + email);

        return ApiResponse.success("Profile updated", mapToResponse(user));
    }

    // ---- UPDATE AVATAR ----
    public ApiResponse<?> updateAvatar(String email, String avatarUrl) {
        UserProfile user = userRepo.findByEmail(email)
                .orElseThrow(() -> new AppException(404, "User not found"));
        user.setAvatarUrl(avatarUrl);
        userRepo.save(user);
        redisTemplate.delete(USER_CACHE + email);
        return ApiResponse.success("Avatar updated", mapToResponse(user));
    }

    // ---- DELETE ACCOUNT ----
    @Transactional
    public ApiResponse<?> deleteAccount(String email) {
        UserProfile user = userRepo.findByEmail(email)
                .orElseThrow(() -> new AppException(404, "User not found"));
        user.setActive(false);
        userRepo.save(user);
        redisTemplate.delete(USER_CACHE + email);
        return ApiResponse.success("Account deleted successfully");
    }

    // ---- ADDRESSES ----
    public ApiResponse<?> getAddresses(String email) {
        UserProfile user = getUserByEmail(email);
        return ApiResponse.success("Addresses fetched", addressRepo.findByUser(user));
    }

    public ApiResponse<?> addAddress(String email, AddressRequest req) {
        UserProfile user = getUserByEmail(email);
        if (req.isDefault()) {
            addressRepo.clearDefaultAddress(user);
        }
        Address address = Address.builder()
                .user(user)
                .label(req.getLabel())
                .street(req.getStreet())
                .city(req.getCity())
                .state(req.getState())
                .zipCode(req.getZipCode())
                .country(req.getCountry())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .isDefault(req.isDefault())
                .build();
        return ApiResponse.success("Address added", addressRepo.save(address));
    }

    public ApiResponse<?> updateAddress(String email, UUID addressId, AddressRequest req) {
        UserProfile user = getUserByEmail(email);
        Address address = addressRepo.findById(addressId)
                .orElseThrow(() -> new AppException(404, "Address not found"));
        if (req.isDefault()) {
            addressRepo.clearDefaultAddress(user);
        }
        address.setLabel(req.getLabel());
        address.setStreet(req.getStreet());
        address.setCity(req.getCity());
        address.setState(req.getState());
        address.setZipCode(req.getZipCode());
        address.setCountry(req.getCountry());
        address.setLatitude(req.getLatitude());
        address.setLongitude(req.getLongitude());
        address.setDefault(req.isDefault());
        return ApiResponse.success("Address updated", addressRepo.save(address));
    }

    public ApiResponse<?> deleteAddress(UUID addressId) {
        addressRepo.findById(addressId)
                .orElseThrow(() -> new AppException(404, "Address not found"));
        addressRepo.deleteById(addressId);
        return ApiResponse.success("Address deleted");
    }

    // ---- FAVORITES ----
    public ApiResponse<?> getFavorites(String email) {
        UserProfile user = getUserByEmail(email);
        return ApiResponse.success("Favorites fetched", favoriteRepo.findByUser(user));
    }

    public ApiResponse<?> addFavorite(String email, FavoriteRequest req) {
        UserProfile user = getUserByEmail(email);
        if (favoriteRepo.findByUserAndRestaurantId(user, req.getRestaurantId()).isPresent()) {
            throw new AppException(400, "Restaurant already in favorites");
        }
        Favorite favorite = Favorite.builder()
                .user(user)
                .restaurantId(req.getRestaurantId())
                .restaurantName(req.getRestaurantName())
                .restaurantImage(req.getRestaurantImage())
                .build();
        return ApiResponse.success("Added to favorites", favoriteRepo.save(favorite));
    }

    @Transactional
    public ApiResponse<?> removeFavorite(String email, UUID restaurantId) {
        UserProfile user = getUserByEmail(email);
        favoriteRepo.deleteByUserAndRestaurantId(user, restaurantId);
        return ApiResponse.success("Removed from favorites");
    }

    // ---- PAYMENT METHODS ----
    public ApiResponse<?> getPaymentMethods(String email) {
        UserProfile user = getUserByEmail(email);
        return ApiResponse.success("Payment methods fetched", paymentRepo.findByUser(user));
    }

    public ApiResponse<?> addPaymentMethod(String email, PaymentMethodRequest req) {
        UserProfile user = getUserByEmail(email);
        if (req.isDefault()) {
            paymentRepo.clearDefaultPayment(user);
        }
        PaymentMethod payment = PaymentMethod.builder()
                .user(user)
                .type(req.getType())
                .provider(req.getProvider())
                .maskedNumber(req.getMaskedNumber())
                .expiryDate(req.getExpiryDate())
                .isDefault(req.isDefault())
                .build();
        return ApiResponse.success("Payment method added", paymentRepo.save(payment));
    }

    public ApiResponse<?> deletePaymentMethod(UUID paymentId) {
        paymentRepo.findById(paymentId)
                .orElseThrow(() -> new AppException(404, "Payment method not found"));
        paymentRepo.deleteById(paymentId);
        return ApiResponse.success("Payment method deleted");
    }

    // ---- HELPERS ----
    private UserProfile getUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new AppException(404, "User not found"));
    }

    private UserProfileResponse mapToResponse(UserProfile user) {
        UserProfileResponse res = new UserProfileResponse();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setPhone(user.getPhone());
        res.setAvatarUrl(user.getAvatarUrl());
        res.setBio(user.getBio());
        res.setRole(user.getRole());
        res.setActive(user.isActive());
        res.setCreatedAt(user.getCreatedAt());
        return res;
    }
}
