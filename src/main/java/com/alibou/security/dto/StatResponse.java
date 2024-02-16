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
public class StatResponse {
	private List<String> label;
	private List<Integer> completed;
	private List<Integer> canceled;
}
