package com.dineout.repository;

import com.dineout.entity.Restaurant;
import com.dineout.enums.CuisineType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Page<Restaurant> findByCity(String city, Pageable pageable);
    Page<Restaurant> findByCuisineType(CuisineType cuisineType, Pageable pageable);
    Page<Restaurant> findByIsActiveTrue(Pageable pageable);
    
    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true " +
           "AND (:city IS NULL OR r.city = :city) " +
           "AND (:cuisineType IS NULL OR r.cuisineType = :cuisineType) " +
           "AND (:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Restaurant> searchRestaurants(
        @Param("city") String city,
        @Param("cuisineType") CuisineType cuisineType,
        @Param("name") String name,
        Pageable pageable
    );
    
    List<Restaurant> findByOwnerId(Long ownerId);
}
