package com.alibou.security.repository;

import com.alibou.security.entity.StocksHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StocksHistoryRepository extends JpaRepository<StocksHistory, Integer> {
    @Query(value="select * from stocks_history where hide = 1 and user_role = ?1 order by id desc", nativeQuery = true)
    Page<StocksHistory> getAllStocksHistory(String fetchType, Pageable pageable);

    @Query(value="select * from stocks_history where hide = 1 and user_role = ?1 order by id asc", nativeQuery = true)
    List<StocksHistory> getAllStocksHistory(String type);

    @Query(value = "select COALESCE(sum(quantity), 0) from stocks_history where name = ?1 and date < ?2 and type = ?3 and hide = 1", nativeQuery = true)
    int calculateInitStockReport(String name, long startDate, String type);

    @Query(value = "select COALESCE(sum(quantity), 0) from stocks_history where name = ?1 and type = ?2 and hide = 1", nativeQuery = true)
    int calculateInitStock(String name, String type);

    @Query(value="select * from stocks_history where order_id = ?1", nativeQuery = true)
    List<StocksHistory> findByOrderId(long orderId);


}
