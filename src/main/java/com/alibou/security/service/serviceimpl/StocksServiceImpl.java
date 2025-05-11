package com.alibou.security.service.serviceimpl;

import com.alibou.security.entity.StocksHistory;
import com.alibou.security.repository.FoodRepository;
import com.alibou.security.repository.StocksHistoryRepository;
import com.alibou.security.service.StocksService;
import com.alibou.security.utils.TelegramService;
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
    private final TelegramService telegramService;

    @Override
    public void updateStocks(int productId, int quantity, double price, String type, String userRole) {
        var food = foodRepository.findFoodById(productId);

        if (userRole.equalsIgnoreCase("staff")) {
            food.setStocks(food.getStocks() + quantity);
            if (type.equalsIgnoreCase("in")) {
                food.setDefaultPrice(price);
            }

            foodRepository.save(food);
        } else if (userRole.equalsIgnoreCase("customer")) {
            food.setStocksCustomer(food.getStocksCustomer() + quantity);
            if (type.equalsIgnoreCase("in")) {
                food.setDefaultPrice(price);
            }

            foodRepository.save(food);
        }



        StocksHistory stocksHistory = new StocksHistory();
        stocksHistory.setDate(System.currentTimeMillis()/1000);
        stocksHistory.setName(food.getName());
        if (type.equalsIgnoreCase("in")) {
            stocksHistory.setPrice(price);
        } else {
            stocksHistory.setPrice(food.getDefaultPrice());
        }
        stocksHistory.setType(type);
        stocksHistory.setQuantity(quantity);
        stocksHistory.setHide(1);
        stocksHistory.setUserRole(userRole);
        stocksHistoryRepository.save(stocksHistory);
    }

    @Override
    public void placeOrderStocks(int productId, int quantity, double price, String type, long orderId, String userRole) {
        var food = foodRepository.findFoodById(productId);

        StocksHistory stocksHistory = new StocksHistory();
        stocksHistory.setDate(System.currentTimeMillis()/1000);
        stocksHistory.setName(food.getName());
        stocksHistory.setQuantity(quantity);
        stocksHistory.setHide(0);
        stocksHistory.setType(type);
        stocksHistory.setOrderId(orderId);
        stocksHistory.setUserRole(userRole);
        stocksHistoryRepository.save(stocksHistory);
    }

    @Override
    public Page<StocksHistory> getAllStocksHistory(String fetchType, Pageable pageable) {
        return stocksHistoryRepository.getAllStocksHistory(fetchType, pageable);
    }

    @Override
    public List<StocksHistory> getStocksHistoryByOrderId(long orderId) {
        return stocksHistoryRepository.findByOrderId(orderId);
    }

    @Override
    public void editStocks(int stockId, int newQuantity, double newPrice, String newType) {
        var stocksHistory = stocksHistoryRepository.findById(stockId);
        if (stocksHistory.isPresent()) {
            if (stocksHistory.get().getType().equalsIgnoreCase("adjustment")) return;
            var oldQuantity = stocksHistory.get().getQuantity();
            var diff = newQuantity - oldQuantity;

            var food = foodRepository.findFoodByName(stocksHistory.get().getName());

            if (food.getType().equalsIgnoreCase("staff")) {
                if (stocksHistory.get().getType().equalsIgnoreCase("out")) {
                    food.setStocks(food.getStocks() - diff);
                } else if (stocksHistory.get().getType().equalsIgnoreCase("in")) {
                    food.setStocks(food.getStocks() + diff);
                }
            } else if (food.getType().equalsIgnoreCase("customer")) {
                if (stocksHistory.get().getType().equalsIgnoreCase("out")) {
                    food.setStocksCustomer(food.getStocksCustomer() - diff);
                } else if (stocksHistory.get().getType().equalsIgnoreCase("in")) {
                    food.setStocksCustomer(food.getStocksCustomer() + diff);
                }
            }

            food.setDefaultPrice(newPrice);
            foodRepository.save(food);

            if (stocksHistory.get().getType().equalsIgnoreCase("in")) {
                stocksHistory.get().setNote("Chỉnh sửa nhập kho từ: " + stocksHistory.get().getQuantity() + " thành: " + newQuantity);
            } else if (stocksHistory.get().getType().equalsIgnoreCase("out")) {
                stocksHistory.get().setNote("Chỉnh sửa xuất kho từ: " + stocksHistory.get().getQuantity() + " thành: " + newQuantity);
            }
            stocksHistory.get().setQuantity(newQuantity);
            stocksHistory.get().setHide(1);
            stocksHistory.get().setDate(System.currentTimeMillis()/1000);

            stocksHistoryRepository.save(stocksHistory.get());
        }
    }

    @Override
    public void checkStocks(String type, String foodName, int realQuantity) {
        var food = foodRepository.findFoodByName(foodName);

        int initialStockIn = 0;
        int initialStockOut = 0;
        int initialStockAdjustment = 0;
        int systemStock = 0;

        if (type.equalsIgnoreCase("staff")) {
            initialStockIn = stocksHistoryRepository.calculateInitStock(food.getName(), "In", "Staff");
            initialStockOut = stocksHistoryRepository.calculateInitStock(food.getName(), "Out", "Staff");
            initialStockAdjustment = stocksHistoryRepository.calculateInitStock(food.getName(), "Adjustment", "Staff");
            systemStock = initialStockIn - initialStockOut + initialStockAdjustment;
        } else if (type.equalsIgnoreCase("customer")) {
            initialStockIn = stocksHistoryRepository.calculateInitStock(food.getName(), "In", "Customer");
            initialStockOut = stocksHistoryRepository.calculateInitStock(food.getName(), "Out", "Customer");
            initialStockAdjustment = stocksHistoryRepository.calculateInitStock(food.getName(), "Adjustment", "Customer");
            systemStock = initialStockIn - initialStockOut + initialStockAdjustment;
        }

        String message = "";

        if (systemStock != realQuantity) {
            StocksHistory stocksHistory = new StocksHistory();
            stocksHistory.setDate(System.currentTimeMillis() / 1000);
            stocksHistory.setName(food.getName());
            stocksHistory.setPrice(0);
            stocksHistory.setType("Adjustment");
            stocksHistory.setHide(1);
            stocksHistory.setUserRole(type);

            if (type.equalsIgnoreCase("staff")) {
                food.setStocks(realQuantity);
            } else if (type.equalsIgnoreCase("customer")) {
                food.setStocksCustomer(realQuantity);
            }
            if (systemStock > realQuantity) {
                // Trừ bớt trong kho - set giá trị âm cho quantity
                int difference = systemStock - realQuantity;
                stocksHistory.setQuantity(-difference);  // Gán giá trị âm cho quantity
                message = "Trừ bớt do chênh lệch " + difference + ", kho có: " + systemStock + " thực có: " + realQuantity;
                stocksHistory.setNote(message);
            } else {
                // Cộng thêm trong kho
                int difference = realQuantity - systemStock;
                stocksHistory.setQuantity(difference);  // Giữ giá trị dương cho quantity
                message = "Tăng thêm do chênh lệch " + difference + ", kho có: " + systemStock + " thực có: " + realQuantity;
                stocksHistory.setNote(message);
            }

            foodRepository.save(food);
            stocksHistoryRepository.save(stocksHistory);

        } else {
            message = "Số liệu khớp, thực có: " + realQuantity + ", kho có: " + systemStock;

        }

        telegramService.sendMessageToGroup("[Kiểm tra kho]\n" + message);
    }

}
