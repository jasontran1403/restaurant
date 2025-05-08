package com.alibou.security.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.alibou.security.dto.BestSellerDto;
import com.alibou.security.dto.OrderRequest;
import com.alibou.security.dto.OrderResponse;
import com.alibou.security.dto.StatResponse;
import com.alibou.security.dto.ToggleOrderRequest;
import com.alibou.security.entity.Order;


public interface OrderService {
	OrderResponse placeOrder(OrderRequest request);
	OrderResponse checkCart(OrderRequest request);
	Order getOrderById(long id);
	Optional<Order> findOrderById(long id);
	List<Order> getAllOrders();
	List<Order> findOrderByStaff(String staffId);
	List<Order> getAllOrdersByStatus(int status);
	Order toggleStatus(long orderId);
	void cancelOrder(long orderId);
	Order toggleOrderStatus(ToggleOrderRequest request);
	Page<Order> getPaginatedOrders(String userRole, Pageable pageable);
	List<Double> getTodayReport(long timeFrom, long timeTo);
	double getHighestOrder(long timeFrom, long timeTo);
	double getLowestOrder(long timeFrom, long timeTo);
	List<BestSellerDto> getBestSellerStat();
	StatResponse getStatByTime(String time);
}
