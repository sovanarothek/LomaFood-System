package com.lomafood.user.repository;

import com.lomafood.user.entity.PaymentMethod;
import com.lomafood.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
    List<PaymentMethod> findByUser(UserProfile user);

    @Modifying
    @Transactional
    @Query("UPDATE PaymentMethod p SET p.isDefault = false WHERE p.user = :user")
    void clearDefaultPayment(UserProfile user);
}
