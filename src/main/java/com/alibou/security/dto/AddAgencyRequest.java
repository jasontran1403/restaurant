package com.alibou.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddAgencyRequest {
	private String username;
	private String password;
	private String phone;
	private String email;
	private String fullname;
}
