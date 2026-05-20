package com.lomafood.restaurant.repository;

import com.lomafood.restaurant.entity.BusinessHours;
import com.lomafood.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BusinessHoursRepository extends JpaRepository<BusinessHours, UUID> {
    List<BusinessHours> findByRestaurant(Restaurant restaurant);
    void deleteByRestaurant(Restaurant restaurant);
}
