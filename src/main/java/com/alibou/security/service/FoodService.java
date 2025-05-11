package com.alibou.security.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.alibou.security.dto.AddFoodRequest;
import com.alibou.security.dto.UpdateFoodRequest;
import com.alibou.security.entity.Food;

public interface FoodService {
	Food addNewFood(AddFoodRequest request, String type);
	Food updateFood(UpdateFoodRequest request);
	Food toggleFoodStatus(long id);
	void removeFood(long id);
	List<Food> getAll();
	List<Food> getAllByType(String type);
	Food getById(long id);
	Page<Food> getPaginatedFoods(String type, Pageable pageable);
	Page<Food> getPaginatedFoodsShow(String type, Pageable pageable);
	Page<Food> findPaginatedFoods(Pageable pageable, String query);
}
