package com.alibou.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCouponRequest {
	private long id;
	private String code;
	private int rate;
	private int status;
}
