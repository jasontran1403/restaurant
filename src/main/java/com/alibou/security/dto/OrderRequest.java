package com.alibou.security.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
	private List<String> food;
	private long codeId;
	private String name;
	private String phone;
	private String address;
	private String message;
	private double rate;
	private String code;
	private String agency;
}
