package com.lomafood.user.repository;

import com.lomafood.user.entity.Favorite;
import com.lomafood.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    List<Favorite> findByUser(UserProfile user);
    Optional<Favorite> findByUserAndRestaurantId(UserProfile user, UUID restaurantId);
    void deleteByUserAndRestaurantId(UserProfile user, UUID restaurantId);
}
