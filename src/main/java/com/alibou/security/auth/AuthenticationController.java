package com.alibou.security.auth;

import java.io.IOException;
import java.util.List;

import com.alibou.security.dto.*;
import com.alibou.security.repository.AgencyRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	private final AgencyRepository agencyRepository;

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
		LoginResponse response = authenticate(loginRequest);
		return ResponseEntity.ok(response);
	}

	public LoginResponse authenticate(LoginRequest loginRequest) {
		// Thực hiện logic xác thực, ví dụ kiểm tra trong cơ sở dữ liệu
		var user = agencyRepository.findByUsername(loginRequest.getUsername());
		if (user.isEmpty()) return new LoginResponse("Agency is not found!");
		if (user.get().getPassword().equals(loginRequest.getPassword())) {
			// Simulate generating a token
			String message = loginRequest.getUsername();
			return new LoginResponse(message);
		} else {
			return new LoginResponse("Invalid username or password");
		}
	}
	
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
		var agency = agencyRepository.findByUsername(orderRequest.getAgency());
		if (agency.isEmpty()) return ResponseEntity.ok("Agency không tồn tại!");
		OrderResponse result = orderService.checkCart(orderRequest);
		if (result.getMessage1().isEmpty() && result.getMessage2().isEmpty()) {
			return ResponseEntity.ok("Đặt thành công!");
		} else {
			if (result.getMessage1().isEmpty()) {
				return ResponseEntity.ok(result.getMessage2() + "giá thay đổi!");
			} else {
				return ResponseEntity.ok(result.getMessage1() + "đã hết!");
			}
		}
	}
	
	@PostMapping("/validate-coupon")
	public ResponseEntity<Double> validateCoupon(@RequestBody ValidateCouponRequest request) {
		if (!request.getUsername().equalsIgnoreCase("LP")) {
			double result = 0;
			return ResponseEntity.ok(result);
		}
		double rate = coupService.validateCoupon(request.getCouponCode());
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
