package com.alibou.security.service.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibou.security.dto.OrderDetailResponse;
import com.alibou.security.dto.OrderRequest;
import com.alibou.security.entity.Food;
import com.alibou.security.entity.OrderDetail;
import com.alibou.security.repository.FoodRepository;
import com.alibou.security.repository.OrderDetailRepository;
import com.alibou.security.service.OrderDetailService;

@Service
public class OrderDetailServiceImpl implements OrderDetailService{
	@Autowired
	OrderDetailRepository orderDetailRepo;
	@Autowired
	FoodRepository foodRepo;

	@Override
	public List<OrderDetail> addOrderDetail(OrderRequest request) {
		// TODO Auto-generated method stub
		List<OrderDetail> results = new ArrayList<>();
		return results;
	}

	@Override
	public List<OrderDetailResponse> getOrderDetailById(long id) {
		// TODO Auto-generated method stub
		List<OrderDetailResponse> results = new ArrayList<>();
		List<OrderDetail> details = orderDetailRepo.getOrderDetailsById(id);
		for (OrderDetail item : details) {
			OrderDetailResponse response = new OrderDetailResponse();
			Food food = foodRepo.findFoodById(item.getFood_id());
			response.setName(food.getName());
			response.setQuantity(item.getQuantity());
			results.add(response);
		}
		
		return results;
	}

}
