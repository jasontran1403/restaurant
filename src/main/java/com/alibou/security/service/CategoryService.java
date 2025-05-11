package com.alibou.security.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.alibou.security.dto.AddCateRequest;
import com.alibou.security.dto.UpdateCateRequest;
import com.alibou.security.entity.Category;

public interface CategoryService {
	Category saveCate(String type, AddCateRequest request);
	void editCate(int id, String name);
	Page<Category> getAllCatesPageable(String type, Pageable pageable);
	List<Category> getAllCatesByType(String type);
	List<Category> getAllCates();
	Category getById(int id);
	void updateCate(UpdateCateRequest request);
	void toggleCateStatus(int id);
}
