package com.alibou.security.service.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.alibou.security.dto.AddReviewRequest;
import com.alibou.security.entity.Food;
import com.alibou.security.entity.Review;
import com.alibou.security.repository.FoodRepository;
import com.alibou.security.repository.ReviewRepository;
import com.alibou.security.service.ImageUploadService;
import com.alibou.security.service.ReviewService;

@Service
public class ReviewServiceImpl implements ReviewService{
	@Autowired
	ReviewRepository reviewRepo;
	
	@Autowired
	FoodRepository foodRepo;
	
	@Autowired
	ImageUploadService uploadService;

	@Override
	public Review addReview(AddReviewRequest request) {
	    // Xử lý lưu đánh giá
	    Review review = new Review();
	    Food food = foodRepo.findById(request.getFoodId()).get();
	    review.setFood(food);
	    review.setName(request.getName());
	    review.setPhone(request.getPhone());
	    review.setReview(request.getReview());
	    review.setStatus(false);
	    
	    // Lưu đánh giá để có Id
	    review = reviewRepo.save(review);
	    
	    // Sau khi đã lưu có Id, cập nhật URL với review.getId()
	    String fileName = "restaurant/restaurant_review_id_" + review.getId();
	    String url = uploadService.uploadImage(request.getImage(), fileName);
	    review.setImage(url);
	    
	    // Lưu lại đánh giá với URL đã cập nhật
	    review = reviewRepo.save(review);
	    
	    // Thêm đánh giá vào danh sách đánh giá của món ăn
	    List<Review> reviews = food.getReviews();
	    reviews.add(review);
	    food.setReviews(reviews);
	    foodRepo.save(food);
	    
	    return review;
	}


	@Override
	public void toggleStatus(long reviewId) {
		// TODO Auto-generated method stub
		Review review = reviewRepo.getById(reviewId);
		boolean status = review.isStatus();
		review.setStatus(!status);
		
		reviewRepo.save(review);
	}

	@Override
	public Page<Review> getReviewByFoodIdAndPageable(Pageable pageable, long foodId) {
		// TODO Auto-generated method stub
		Page<Review> result = reviewRepo.findReviewsPageableByFoodId(pageable, foodId);
		return result;
	}

}
