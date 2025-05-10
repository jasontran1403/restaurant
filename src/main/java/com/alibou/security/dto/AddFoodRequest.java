package com.alibou.security.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddFoodRequest {
	private String name;
	private String description;
	private double price;
	private String quantity;
	private int status;
	private List<Integer> categories;
	private MultipartFile image;
}
