package com.saas.billing.repository;

import com.saas.billing.entity.CouponCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CouponCodeRepository extends JpaRepository<CouponCode, UUID> {
    Optional<CouponCode> findByCode(String code);
}
