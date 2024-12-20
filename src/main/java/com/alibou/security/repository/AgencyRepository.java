package com.alibou.security.repository;

import com.alibou.security.entity.Agency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AgencyRepository extends JpaRepository<Agency, Integer> {
    @Query(value="select * from agency where username = ?1", nativeQuery=true)
    Optional<Agency> findByUsername(String username);
}
