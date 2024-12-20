package com.alibou.security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String phone;
    @OneToOne
    @JoinColumn(name = "f_1_id")
    private Agency f1;
    @OneToOne
    @JoinColumn(name = "f_2_id")
    private Agency f2;
}
