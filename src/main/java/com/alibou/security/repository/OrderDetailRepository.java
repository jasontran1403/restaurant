package com.alibou.security.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.alibou.security.entity.OrderDetail;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long>{
	@Query(value="select * from order_details where order_id = ?1", nativeQuery=true)
	List<OrderDetail> getOrderDetailsById(long id);
	
	@Query(value="select * from order_details where food_id = ?1", nativeQuery=true)
	List<OrderDetail> getOrderDetailsByFoodId(long id);
}
