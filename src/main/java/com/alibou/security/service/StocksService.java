package com.alibou.security.service;

import com.alibou.security.entity.StocksHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StocksService {
    void updateStocks(int productId, int quantity, double price, String type, String userRole);
    void placeOrderStocks(int productId, int quantity, double price, String type, long orderId, String userRole);
    Page<StocksHistory> getAllStocksHistory(String fetchType, Pageable pageable);
    List<StocksHistory> getStocksHistoryByOrderId(long orderId);
    void editStocks(int stockId, int newQuantity, double newPrice, String newType);
    void checkStocks(String foodName, int realQuantity);
}
