package com.alibou.security.service.serviceimpl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.alibou.security.dto.CouponRequest;
import com.alibou.security.dto.UpdateCouponRequest;
import com.alibou.security.entity.Coupon;
import com.alibou.security.repository.CouponRepository;
import com.alibou.security.service.CouponService;

@Service
public class CouponServiceImpl implements CouponService {
	@Autowired
	CouponRepository coupRepo;

	@Override
	public Coupon addCoupon(CouponRequest request) {
		// TODO Auto-generated method stub
		Optional<Coupon> coupon = coupRepo.getByCode(request.getCode());
		if (coupon.isPresent()) {
			return null;
		}
		Coupon newCoupon = new Coupon();
		newCoupon.setCode(request.getCode());
		newCoupon.setRate(request.getRate());
		newCoupon.setStatus(1);
		return coupRepo.save(newCoupon);
	}

	@Override
	public double validateCoupon(String code) {
		// TODO Auto-generated method stub
		Optional<Coupon> coupon = coupRepo.getByCode(code);
		double rate = 0.0;
		if (coupon.isEmpty()) {
			rate = 0.0;
			return rate;
		} else {
			if (coupon.get().getStatus() == 1) {
				rate = -1;
				return rate;
			} else {
				rate = coupon.get().getRate();
				return rate;
			}
			
		}
	}

	@Override
	public void toggleStatus(long id) {
		// TODO Auto-generated method stub
		Coupon coupon = coupRepo.findById(id).get();
		if (coupon.getStatus() == 0) {
			coupon.setStatus(1);
		} else {
			coupon.setStatus(0);
		}
		coupRepo.save(coupon);
	}

	@Override
	public Page<Coupon> getCouponPaginate(Pageable pageable) {
		// TODO Auto-generated method stub
		return coupRepo.findAll(pageable);
	}

	@Override
	public void updateCoupon(UpdateCouponRequest request) {
		// TODO Auto-generated method stub
		Coupon coupon = coupRepo.getById(request.getId());
		coupon.setCode(request.getCode());
		coupon.setRate(request.getRate());
		coupon.setStatus(request.getStatus());
		coupRepo.save(coupon);
	}

}
