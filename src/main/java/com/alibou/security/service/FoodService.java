package com.alibou.security.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.alibou.security.dto.AddFoodRequest;
import com.alibou.security.dto.UpdateFoodRequest;
import com.alibou.security.entity.Food;

public interface FoodService {
	Food addNewFood(AddFoodRequest request);
	Food updateFood(UpdateFoodRequest request);
	Food toggleFoodStatus(long id);
	void removeFood(long id);
	List<Food> getAll();
	Food getById(long id);
	Page<Food> getPaginatedFoods(Pageable pageable);
	Page<Food> getPaginatedFoodsShow(Pageable pageable);
	Page<Food> findPaginatedFoods(Pageable pageable, String query);
}
