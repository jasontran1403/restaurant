package com.alibou.security.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alibou.security.entity.CategoryFood;

public interface CategoryFoodRepository extends JpaRepository<CategoryFood, Long> {
    List<CategoryFood> findByFoodId(Long foodId);
}
