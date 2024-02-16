package com.alibou.security.auth;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibou.security.dto.OrderDetailResponse;
import com.alibou.security.dto.OrderRequest;
import com.alibou.security.dto.OrderResponse;
import com.alibou.security.dto.StatResponse;
import com.alibou.security.entity.Food;
import com.alibou.security.service.CouponService;
import com.alibou.security.service.FoodService;
import com.alibou.security.service.OrderDetailService;
import com.alibou.security.service.OrderService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

	private final AuthenticationService service;
	private final OrderService orderService;
	private final OrderDetailService orderDetailService;
	private final FoodService foodService;
	private final CouponService coupService;
	
	@GetMapping("/order-detail/{id}")
	public List<OrderDetailResponse> orderDetail(@PathVariable Long id) {
		List<OrderDetailResponse> orderDetails = orderDetailService.getOrderDetailById(id);
		return orderDetails;
	}
	
	@GetMapping("/stat/{date}")
	public StatResponse start(@PathVariable("date") String date) {
		StatResponse response = orderService.getStatByTime(date);
		return response;
	}
	
	@PostMapping("/check-cart")
	public ResponseEntity<String> checkCart(@RequestBody OrderRequest orderRequest) {
		OrderResponse result = orderService.checkCart(orderRequest);
		if (result.getMessage1().equals("") && result.getMessage2().equals("")) {
			return ResponseEntity.ok("Đặt thành công!");
		} else {
			if (result.getMessage1().equals("")) {
				
				return ResponseEntity.ok(result.getMessage2() + "giá thay đổi!");
			} else {
				return ResponseEntity.ok(result.getMessage1() + "đã hết!");
			}
		}
	}
	
	@GetMapping("/validate-coupon/{code}")
	public ResponseEntity<Double> validateCoupon(@PathVariable("code") String code) {
		double rate = coupService.validateCoupon(code);
		return ResponseEntity.ok(rate);
	}

	@PostMapping("/register")
	public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
		return ResponseEntity.ok(service.register(request));
	}
	
	@PostMapping("/order")
	public ResponseEntity<OrderResponse> register(@RequestBody OrderRequest request) {
		return ResponseEntity.ok(orderService.placeOrder(request));
	}
	
	@GetMapping("/get-food")
	public ResponseEntity<List<Food>> getAllFood() {
		return ResponseEntity.ok(foodService.getAll());
	}

	@PostMapping("/authenticate")
	public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
		return ResponseEntity.ok(service.authenticate(request));
	}

	@PostMapping("/refresh-token")
	public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
		service.refreshToken(request, response);
	}

}
