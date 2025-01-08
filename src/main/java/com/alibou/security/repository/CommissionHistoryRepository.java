package com.alibou.security.repository;

import com.alibou.security.entity.Agency;
import com.alibou.security.entity.CommissionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CommissionHistoryRepository extends JpaRepository<CommissionHistory, Integer> {
    @Query(value="select * from commisison_history where from_order = ?1", nativeQuery = true)
    Optional<CommissionHistory> findCommissionHistoryByOrderId(long orderId);
}
