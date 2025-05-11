package com.alibou.security.repository;

import com.alibou.security.entity.Agency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AgencyRepository extends JpaRepository<Agency, Integer> {
    @Query(value="select * from agency where username = ?1", nativeQuery=true)
    Optional<Agency> findByUsername(String username);

    @Query(value="select * from agency where email = ?1", nativeQuery=true)
    Optional<Agency> findByEmail(String email);

    @Query(value="select * from agency where role = ?1", nativeQuery=true)
    Page<Agency> findAllByType(String type, Pageable pageable);
}
