package com.alibou.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EditStocksRequest {
    private int newId;
    private double newPrice;
    private int newQuantity;
    private String newType;
}
