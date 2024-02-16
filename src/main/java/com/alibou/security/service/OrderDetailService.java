package com.alibou.security.service;

import java.util.List;

import com.alibou.security.dto.OrderDetailResponse;
import com.alibou.security.dto.OrderRequest;
import com.alibou.security.entity.OrderDetail;

public interface OrderDetailService {
	List<OrderDetail> addOrderDetail(OrderRequest request);
	List<OrderDetailResponse> getOrderDetailById(long id);
}
