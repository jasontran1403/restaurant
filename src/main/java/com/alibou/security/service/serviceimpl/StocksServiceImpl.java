package com.alibou.security.service.serviceimpl;

import com.alibou.security.entity.StocksHistory;
import com.alibou.security.repository.FoodRepository;
import com.alibou.security.repository.StocksHistoryRepository;
import com.alibou.security.service.StocksService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StocksServiceImpl implements StocksService {
    private final StocksHistoryRepository stocksHistoryRepository;
    private final FoodRepository foodRepository;

    @Override
    public void updateStocks(int productId, int quantity, double price, String type) {
        var food = foodRepository.findFoodById(productId);
        food.setStocks(food.getStocks() + quantity);
        food.setDefaultPrice(price);
        foodRepository.save(food);

        StocksHistory stocksHistory = new StocksHistory();
        stocksHistory.setDate(System.currentTimeMillis()/1000);
        stocksHistory.setName(food.getName());
        stocksHistory.setPrice(price);
        stocksHistory.setType(type);
        stocksHistory.setQuantity(quantity);
        stocksHistoryRepository.save(stocksHistory);
    }

    @Override
    public Page<StocksHistory> getAllStocksHistory(Pageable pageable) {
        return stocksHistoryRepository.getAllStocksHistory(pageable);
    }

    @Override
    public List<StocksHistory> getHistoryByDateRange(long startDate, long endDate) {
        return stocksHistoryRepository.findAllByDateBetween(startDate, endDate);
    }

    @Override
    public void editStocks(int stockId, int newQuantity, double newPrice, String newType) {
        var stocksHistory = stocksHistoryRepository.findById(stockId);
        if (stocksHistory.isPresent()) {
            var food = foodRepository.findFoodByName(stocksHistory.get().getName());
            food.setDefaultPrice(newPrice);
            foodRepository.save(food);

            stocksHistory.get().setQuantity(newQuantity);
            stocksHistory.get().setPrice(newPrice);
            stocksHistory.get().setType(newType);
            stocksHistory.get().setNote("Adjustment");
            stocksHistory.get().setDate(System.currentTimeMillis()/1000);

            stocksHistoryRepository.save(stocksHistory.get());
        }
    }
}
