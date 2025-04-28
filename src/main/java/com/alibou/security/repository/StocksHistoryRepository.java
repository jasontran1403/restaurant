package com.alibou.security.repository;

import com.alibou.security.entity.StocksHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StocksHistoryRepository extends JpaRepository<StocksHistory, Integer> {
    @Query(value="select * from stocks_history order by id desc", nativeQuery = true)
    Page<StocksHistory> getAllStocksHistory(Pageable pageable);

    List<StocksHistory> findAllByDateBetween(long startDate, long endDate);

    List<StocksHistory> findByDateBetween(long dateStart, long dateEnd);

    @Query(value = "select COALESCE(sum(quantity), 0) from stocks_history where name = ?1 and date < ?2 and type = ?3", nativeQuery = true)
    int calculateInitStockReport(String name, long startDate, String type);
}
