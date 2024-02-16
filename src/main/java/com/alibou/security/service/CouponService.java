package com.alibou.security.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.alibou.security.dto.CouponRequest;
import com.alibou.security.dto.UpdateCouponRequest;
import com.alibou.security.entity.Coupon;

public interface CouponService {
	Coupon addCoupon(CouponRequest request);
	double validateCoupon(String code);
	void toggleStatus(long id);
	Page<Coupon> getCouponPaginate(Pageable pageable);
	void updateCoupon(UpdateCouponRequest request);
}
