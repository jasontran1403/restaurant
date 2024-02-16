package com.alibou.security.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.alibou.security.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long>{
	@Query(value = "SELECT r FROM Review r WHERE r.food.id = ?1",
	           countQuery = "SELECT r FROM Review r WHERE r.food.id = ?1",
	           nativeQuery = false)
	Page<Review> findReviewsPageableByFoodId(Pageable pageable, long foodId);
}
