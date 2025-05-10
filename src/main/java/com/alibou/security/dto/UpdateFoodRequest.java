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
public class UpdateFoodRequest {
	private long id;
	private String name;
	private String description;
	private double price;
	private List<Integer> categories;
	private int status;
	private String quantity;
	private MultipartFile image;
}
