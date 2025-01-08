package com.alibou.security.service;

import com.alibou.security.entity.StocksHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StocksService {
    void updateStocks(int productId, int quantity, double price, String type);
    Page<StocksHistory> getAllStocksHistory(Pageable pageable);
    List<StocksHistory> getHistoryByDateRange(long startDate, long endDate);
    void editStocks(int stockId, int newQuantity, double newPrice, String newType);
}
