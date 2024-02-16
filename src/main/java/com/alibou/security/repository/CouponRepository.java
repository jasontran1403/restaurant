package com.alibou.security.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.alibou.security.entity.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long>{
	@Query(value="select * from coupon where code = ?1", nativeQuery=true)
	Optional<Coupon> getByCode(String code);
}
