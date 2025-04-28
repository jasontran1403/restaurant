package com.alibou.security.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockReportRow {
    private String itemName;
    private int stockAtA;
    private Map<Long, Integer> dailyChanges = new LinkedHashMap<>();
    private Map<Long, Integer> dailyAdjustments = new LinkedHashMap<>();
    private int stockAtC;
    private int totalAdjustments;
}
