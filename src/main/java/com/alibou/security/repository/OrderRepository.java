package com.alibou.security.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.alibou.security.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long>{
	@Query(value="select * from orders where status = ?1", nativeQuery=true)
	List<Order> findOrderByStatus(int status);

	@Query(value="select * from orders where staff = ?1 order by id desc", nativeQuery=true)
	List<Order> findOrdersByStaff(String staff);
	
	@Query(value="select * from orders where user_role = ?1 order by time DESC", nativeQuery=true)
	Page<Order> findAllPagable(String userRole, Pageable pageable);
	
	@Query(value="select * from orders where time >= ?1 and time <= ?2 order by time", nativeQuery=true)
	List<Order> getOrderByTimeRange(long timeFrom, long timeTo);
}
