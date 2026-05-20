package com.dineout.service;

import com.dineout.dto.RestaurantRequest;
import com.dineout.dto.RestaurantResponse;
import com.dineout.entity.Restaurant;
import com.dineout.entity.User;
import com.dineout.enums.CuisineType;
import com.dineout.repository.RestaurantRepository;
import com.dineout.repository.ReviewRepository;
import com.dineout.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class RestaurantService {
    
    @Autowired
    private RestaurantRepository restaurantRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Transactional
    @CacheEvict(value = "restaurants", allEntries = true)
    public RestaurantResponse createRestaurant(RestaurantRequest request) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User owner = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .cuisineType(request.getCuisineType())
                .openingTime(request.getOpeningTime())
                .closingTime(request.getClosingTime())
                .capacity(request.getCapacity())
                .averageCostForTwo(request.getAverageCostForTwo())
                .rating(BigDecimal.ZERO)
                .acceptsReservations(true)
                .isActive(true)
                .owner(owner)
                .imageUrl(request.getImageUrl())
                .build();
        
        restaurant = restaurantRepository.save(restaurant);
        return mapToResponse(restaurant);
    }
    
    @Cacheable(value = "restaurants", key = "#id")
    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
        return mapToResponse(restaurant);
    }
    
    @Cacheable(value = "restaurants", key = "'search-' + #city + '-' + #cuisineType + '-' + #name + '-' + #pageable.pageNumber")
    public Page<RestaurantResponse> searchRestaurants(String city, CuisineType cuisineType, 
                                                      String name, Pageable pageable) {
        return restaurantRepository.searchRestaurants(city, cuisineType, name, pageable)
                .map(this::mapToResponse);
    }
    
    @Transactional
    @CacheEvict(value = "restaurants", allEntries = true)
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
        
        // Verify ownership
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!restaurant.getOwner().getEmail().equals(currentUserEmail)) {
            throw new RuntimeException("You are not authorized to update this restaurant");
        }
        
        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(request.getCity());
        restaurant.setState(request.getState());
        restaurant.setZipCode(request.getZipCode());
        restaurant.setPhoneNumber(request.getPhoneNumber());
        restaurant.setEmail(request.getEmail());
        restaurant.setCuisineType(request.getCuisineType());
        restaurant.setOpeningTime(request.getOpeningTime());
        restaurant.setClosingTime(request.getClosingTime());
        restaurant.setCapacity(request.getCapacity());
        restaurant.setAverageCostForTwo(request.getAverageCostForTwo());
        restaurant.setImageUrl(request.getImageUrl());
        
        restaurant = restaurantRepository.save(restaurant);
        return mapToResponse(restaurant);
    }
    
    @Transactional
    @CacheEvict(value = "restaurants", allEntries = true)
    public void deleteRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
        
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!restaurant.getOwner().getEmail().equals(currentUserEmail)) {
            throw new RuntimeException("You are not authorized to delete this restaurant");
        }
        
        restaurant.setIsActive(false);
        restaurantRepository.save(restaurant);
    }
    
    private RestaurantResponse mapToResponse(Restaurant restaurant) {
        Long reviewCount = reviewRepository.getReviewCountByRestaurantId(restaurant.getId());
        
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .address(restaurant.getAddress())
                .city(restaurant.getCity())
                .state(restaurant.getState())
                .zipCode(restaurant.getZipCode())
                .phoneNumber(restaurant.getPhoneNumber())
                .email(restaurant.getEmail())
                .cuisineType(restaurant.getCuisineType())
                .openingTime(restaurant.getOpeningTime())
                .closingTime(restaurant.getClosingTime())
                .capacity(restaurant.getCapacity())
                .rating(restaurant.getRating())
                .averageCostForTwo(restaurant.getAverageCostForTwo())
                .acceptsReservations(restaurant.getAcceptsReservations())
                .imageUrl(restaurant.getImageUrl())
                .reviewCount(reviewCount)
                .build();
    }
}
