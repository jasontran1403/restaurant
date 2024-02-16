package com.alibou.security.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alibou.security.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
	Optional<Category> findByCateName(String name);
}
