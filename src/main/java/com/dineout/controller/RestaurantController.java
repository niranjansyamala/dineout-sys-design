package com.dineout.controller;

import com.dineout.dto.RestaurantRequest;
import com.dineout.dto.RestaurantResponse;
import com.dineout.enums.CuisineType;
import com.dineout.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurants")
@Tag(name = "Restaurants", description = "Restaurant management APIs")
public class RestaurantController {
    
    @Autowired
    private RestaurantService restaurantService;
    
    @PostMapping
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create a new restaurant", description = "Restaurant owners can create new restaurants")
    public ResponseEntity<RestaurantResponse> createRestaurant(@Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.createRestaurant(request));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get restaurant by ID", description = "Retrieve restaurant details by ID")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search restaurants", description = "Search restaurants by city, cuisine type, and name")
    public ResponseEntity<Page<RestaurantResponse>> searchRestaurants(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) CuisineType cuisineType,
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return ResponseEntity.ok(restaurantService.searchRestaurants(city, cuisineType, name, pageable));
    }
    
    @PutMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update restaurant", description = "Update restaurant details")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(id, request));
    }
    
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete restaurant", description = "Soft delete a restaurant")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.noContent().build();
    }
}
