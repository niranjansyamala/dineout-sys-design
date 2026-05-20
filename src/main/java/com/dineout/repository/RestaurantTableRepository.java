package com.dineout.repository;

import com.dineout.entity.RestaurantTable;
import com.dineout.enums.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    List<RestaurantTable> findByRestaurantId(Long restaurantId);
    
    List<RestaurantTable> findByRestaurantIdAndStatus(Long restaurantId, TableStatus status);
    
    List<RestaurantTable> findByRestaurantIdAndCapacityGreaterThanEqual(
        Long restaurantId, 
        Integer capacity
    );
}
