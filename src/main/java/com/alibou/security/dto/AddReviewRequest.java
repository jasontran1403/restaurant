package com.alibou.security.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddReviewRequest {
	private long foodId;
	private String review;
	private String name;
	private String phone;
	private MultipartFile image;
}
