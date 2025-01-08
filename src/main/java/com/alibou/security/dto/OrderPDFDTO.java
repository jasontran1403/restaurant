package com.alibou.security.dto;

import com.alibou.security.entity.Order;
import com.alibou.security.entity.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderPDFDTO {
    Order order;
    String name;
    long date;
    List<OrderDetail> orderDetails;
}
