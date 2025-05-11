package com.alibou.security.restcontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibou.security.dto.AddFoodRequest;
import com.alibou.security.dto.ToggleFoodRequest;
import com.alibou.security.dto.ToggleOrderRequest;
import com.alibou.security.dto.UpdateFoodRequest;
import com.alibou.security.entity.Food;
import com.alibou.security.entity.Order;
import com.alibou.security.service.FoodService;
import com.alibou.security.service.OrderService;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
	@Autowired
	FoodService foodService;
	
	@Autowired
	OrderService orderService;

    @GetMapping("/get-order")
    public ResponseEntity<List<Order>> get() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
    
    @GetMapping("/get-order/status={status}")
    public ResponseEntity<List<Order>> getByStatus(@PathVariable("status") int status) {
        return ResponseEntity.ok(orderService.getAllOrdersByStatus(status));
    }
    
    @PutMapping("/toggle-order")
    public ResponseEntity<Order> toggleOrder(@RequestBody ToggleOrderRequest request) {
        return ResponseEntity.ok(orderService.toggleOrderStatus(request));
    }
    
    @PostMapping("/create")
    public ResponseEntity<Food> post(@RequestBody AddFoodRequest request) {
        return ResponseEntity.ok(foodService.addNewFood(request, "Staff"));
    }
    @PutMapping("/update")
    public ResponseEntity<Food> update(@RequestBody UpdateFoodRequest request) {
        return ResponseEntity.ok(foodService.updateFood(request));
    }
    
    @PutMapping("/toggle")
    public ResponseEntity<Food> toggle(@RequestBody ToggleFoodRequest request) {
        return ResponseEntity.ok(foodService.toggleFoodStatus(request.getId()));
    }
    @DeleteMapping
    public ResponseEntity<String> delete() {
        return ResponseEntity.ok("Remove");
    }
}
