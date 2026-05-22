package com.lomafood.rider.repository;

import com.lomafood.rider.entity.Rider;
import com.lomafood.rider.entity.RiderAvailability;
import com.lomafood.rider.entity.RiderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RiderRepository extends JpaRepository<Rider, UUID> {
    Optional<Rider> findByEmail(String email);
    List<Rider> findByStatus(RiderStatus status);
    List<Rider> findByAvailability(RiderAvailability availability);
    List<Rider> findByStatusAndAvailability(RiderStatus status, RiderAvailability availability);
}
