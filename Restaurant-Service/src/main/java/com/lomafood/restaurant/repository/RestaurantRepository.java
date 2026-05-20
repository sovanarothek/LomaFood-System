package com.lomafood.restaurant.repository;

import com.lomafood.restaurant.entity.Restaurant;
import com.lomafood.restaurant.entity.RestaurantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {
    List<Restaurant> findByStatus(RestaurantStatus status);
    List<Restaurant> findByOwnerEmail(String ownerEmail);
    Optional<Restaurant> findByIdAndOwnerEmail(UUID id, String ownerEmail);
    List<Restaurant> findByOpenTrue();
    List<Restaurant> findByCityAndOpenTrue(String city);
}
