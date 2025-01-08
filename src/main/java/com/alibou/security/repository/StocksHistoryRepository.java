package com.alibou.security.repository;

import com.alibou.security.entity.StocksHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StocksHistoryRepository extends JpaRepository<StocksHistory, Integer> {
    @Query(value="select * from stocks_history order by id", nativeQuery = true)
    Page<StocksHistory> getAllStocksHistory(Pageable pageable);

    List<StocksHistory> findAllByDateBetween(long startDate, long endDate);
}
