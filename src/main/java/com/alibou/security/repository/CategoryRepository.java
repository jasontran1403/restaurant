package com.alibou.security.repository;

import java.util.List;
import java.util.Optional;

import com.alibou.security.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.alibou.security.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
	Optional<Category> findByCateName(String name);

	Page<Category> findAllByType(String type, Pageable pageable);
	List<Category> getAllByType(String type);
}
