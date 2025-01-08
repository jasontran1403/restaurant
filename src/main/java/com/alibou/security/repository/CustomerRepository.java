package com.alibou.security.repository;

import com.alibou.security.entity.Agency;
import com.alibou.security.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    @Query(value="select * from customer where phone = ?1", nativeQuery=true)
    Optional<Customer> findByUsername(String phone);

    Page<Customer> findAll(Pageable pageable);
}
