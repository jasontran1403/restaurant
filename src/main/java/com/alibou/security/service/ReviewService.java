package com.alibou.security.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.alibou.security.dto.AddReviewRequest;
import com.alibou.security.entity.Review;

public interface ReviewService {
	Review addReview(AddReviewRequest request);
	void toggleStatus(long reviewId);
	Page<Review> getReviewByFoodIdAndPageable(Pageable pageable, long foodId);
}
