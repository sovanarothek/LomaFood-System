package com.lomafood.restaurant.repository;

import com.lomafood.restaurant.entity.MenuItem;
import com.lomafood.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
    List<MenuItem> findByRestaurant(Restaurant restaurant);
    List<MenuItem> findByRestaurantAndAvailableTrue(Restaurant restaurant);
    List<MenuItem> findByRestaurantAndCategory(Restaurant restaurant, String category);
    List<MenuItem> findByRestaurantAndPopularTrue(Restaurant restaurant);
}
