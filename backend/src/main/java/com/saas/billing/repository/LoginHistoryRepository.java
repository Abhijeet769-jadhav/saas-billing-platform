package com.saas.billing.repository;

import com.saas.billing.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, UUID> {
    List<LoginHistory> findByUserIdOrderByLoginTimeDesc(UUID userId);
}
