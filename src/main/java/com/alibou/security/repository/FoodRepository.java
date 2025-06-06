package com.alibou.security.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.alibou.security.entity.Food;

public interface FoodRepository extends JpaRepository<Food, Long>{
	@Query(value="select * from food where id = ?1", nativeQuery = true)
	Food findFoodById(long id);

	@Query(value="select * from food where name = ?1", nativeQuery = true)
	Food findFoodByName(String name);
	
	@Query(value="select * from food where id = ?1", nativeQuery = true)
	Optional<Food> getFoodById(long id);
	
	@Query(value = "SELECT f FROM Food f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', ?1, '%')) and f.status = 0",
	           countQuery = "SELECT COUNT(f) FROM Food f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', ?1, '%')) and f.status = 0",
	           nativeQuery = false)
	Page<Food> findPaginatedFoods(Pageable pageable, String query);
	
	@Query(value = "SELECT f FROM Food f WHERE f.status = 0  and f.type = :type",
	           countQuery = "SELECT f FROM Food f WHERE f.status = 0",
	           nativeQuery = false)
	Page<Food> findAllActive(String type, Pageable pageable);

	@Query(value = "SELECT f FROM Food f WHERE f.type = :type",
			countQuery = "SELECT f FROM Food f WHERE f.status = 0",
			nativeQuery = false)
	Page<Food> findAllByType(String type, Pageable pageable);

	@Query(value = "SELECT f FROM Food f WHERE f.type = :type",
			countQuery = "SELECT f FROM Food f WHERE f.status = 0",
			nativeQuery = false)
	List<Food> findAllFoodByType(String type);
}
