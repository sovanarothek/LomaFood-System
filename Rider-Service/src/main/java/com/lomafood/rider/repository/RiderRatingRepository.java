package com.lomafood.rider.repository;

import com.lomafood.rider.entity.RiderRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface RiderRatingRepository extends JpaRepository<RiderRating, UUID> {
    List<RiderRating> findByRiderId(UUID riderId);

    @Query("SELECT AVG(r.rating) FROM RiderRating r WHERE r.riderId = :riderId")
    Double getAverageRating(UUID riderId);
}
