package com.alibou.security.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.alibou.security.dto.AddCateRequest;
import com.alibou.security.dto.UpdateCateRequest;
import com.alibou.security.entity.Category;

public interface CategoryService {
	Category saveCate(AddCateRequest request);
	void editCate(int id, String name);
	Page<Category> getAllCatesPageable(Pageable pageable);
	List<Category> getAllCates();
	Category getById(int id);
	void updateCate(UpdateCateRequest request);
	void toggleCateStatus(int id);
}
