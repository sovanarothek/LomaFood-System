package com.lomafood.rider.repository;

import com.lomafood.rider.entity.Earning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface EarningRepository extends JpaRepository<Earning, UUID> {
    List<Earning> findByRiderId(UUID riderId);
    List<Earning> findByRiderIdAndPaid(UUID riderId, boolean paid);

    @Query("SELECT SUM(e.amount) FROM Earning e WHERE e.riderId = :riderId AND e.paid = false")
    Double getTotalUnpaidEarnings(UUID riderId);
}
