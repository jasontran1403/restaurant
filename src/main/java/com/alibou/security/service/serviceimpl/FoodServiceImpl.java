package com.alibou.security.service.serviceimpl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.alibou.security.dto.AddFoodRequest;
import com.alibou.security.dto.UpdateFoodRequest;
import com.alibou.security.entity.Category;
import com.alibou.security.entity.Food;
import com.alibou.security.repository.CategoryFoodRepository;
import com.alibou.security.repository.CategoryRepository;
import com.alibou.security.repository.FoodRepository;
import com.alibou.security.service.FoodService;
import com.alibou.security.service.ImageUploadService;

@Service
public class FoodServiceImpl implements FoodService{
	@Autowired
	FoodRepository foodRepo;
	
	@Autowired
	CategoryRepository cateRepo;
	
	@Autowired
	CategoryFoodRepository cateFoodRepo;
	
	@Autowired
	ImageUploadService uploadService;

	@Override
	public Food addNewFood(AddFoodRequest request) {
		// TODO Auto-generated method stub
		List<String> cates = new ArrayList<>();
		for (Integer cateId : request.getCategories()) {
			Category cate = cateRepo.getById(cateId);
			cates.add(cate.getCateName());
		}
		Food food = new Food();
		
		
		food.setName(request.getName());
		food.setDescription(request.getDescription());
		food.setPrice(request.getPrice());
		food.setStatus(request.getStatus());
		food.setImage("");
		food.setCategories(cates);
		food.setStocks(0);
		if (request.getQuantity().isEmpty()) {
			food.setQuantity("Piece");
		} else {
			food.setQuantity(request.getQuantity());
		}
		Food result = foodRepo.save(food);
		
		String fileName = "restaurant/restaurant_food_id_" + food.getId();
		String url = uploadService.uploadImage(request.getImage(), fileName);
		result.setImage(url);
		
	
		return foodRepo.save(result);
	}

	@Override
	public Food updateFood(UpdateFoodRequest request) {
		List<String> cates = new ArrayList<>();
		for (Integer cateId : request.getCategories()) {
			Category cate = cateRepo.getById(cateId);
			cates.add(cate.getCateName());
		}
		Food food = foodRepo.getById(request.getId());
		food.setName(request.getName());
		food.setDescription(request.getDescription());
		food.setPrice(request.getPrice());
		food.setStatus(request.getStatus());
		food.setCategories(cates);
		if (!request.getQuantity().isEmpty()) {
			food.setQuantity(request.getQuantity());
		}
		if (!request.getImage().isEmpty()) {
			String fileName = "restaurant/restaurant_food_id_" + food.getId();
			String url = uploadService.uploadImage(request.getImage(), fileName);
			food.setImage(url);;
		} else {
			food.setImage(food.getImage());
		}
		
		return foodRepo.save(food);
	}

	@Override
	public Food toggleFoodStatus(long id) {
		// TODO Auto-generated method stub
		Food food = foodRepo.getById(id);
		int status = food.getStatus();
	    if (status == 0) {
	    	status = 1;
	    }else {
	    	status = 0;
	    }
	    food.setStatus(status);
		
		return foodRepo.save(food);
	}

	@Override
	public void removeFood(long id) {
		// TODO Auto-generated method stub
		Food food = foodRepo.getById(id);
		food.setStatus(3);
		
		foodRepo.save(food);
	}

	@Override
	public List<Food> getAll() {
		// TODO Auto-generated method stub
		return foodRepo.findAll();
	}

	@Override
	public Food getById(long id) {
		// TODO Auto-generated method stub
		Food food = foodRepo.findFoodById(id);
		return food;
	}

	@Override
	public Page<Food> getPaginatedFoods(Pageable pageable) {
		// TODO Auto-generated method stub
		return foodRepo.findAll(pageable);
	}

	@Override
	public Page<Food> findPaginatedFoods(Pageable pageable, String query) {
		// TODO Auto-generated method stub
		return foodRepo.findPaginatedFoods(pageable, query);
	}

	@Override
	public Page<Food> getPaginatedFoodsShow(Pageable pageable) {
		// TODO Auto-generated method stub
		return foodRepo.findAllActive(pageable);
	}

}
