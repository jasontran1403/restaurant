package com.alibou.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailDTO {
    private long id;
    private String name;
    private String phone;
    private String address;
    private double total;
    private double actual;
    private double vat;
    private String time;
    private String userRole;
    private int status;
    private double commission;
}
