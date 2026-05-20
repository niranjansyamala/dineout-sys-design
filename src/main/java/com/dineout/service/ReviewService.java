package com.dineout.service;

import com.dineout.dto.ReviewRequest;
import com.dineout.dto.ReviewResponse;
import com.dineout.entity.Booking;
import com.dineout.entity.Restaurant;
import com.dineout.entity.Review;
import com.dineout.entity.User;
import com.dineout.enums.BookingStatus;
import com.dineout.repository.BookingRepository;
import com.dineout.repository.RestaurantRepository;
import com.dineout.repository.ReviewRepository;
import com.dineout.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private RestaurantRepository restaurantRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        
        Booking booking = null;
        boolean verified = false;
        
        if (request.getBookingId() != null) {
            booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            
            // Check if booking belongs to user and is completed
            if (!booking.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Booking does not belong to you");
            }
            
            if (booking.getStatus() == BookingStatus.COMPLETED) {
                verified = true;
            }
        }
        
        Review review = Review.builder()
                .user(user)
                .restaurant(restaurant)
                .booking(booking)
                .rating(request.getRating())
                .comment(request.getComment())
                .verified(verified)
                .build();
        
        review = reviewRepository.save(review);
        
        // Update restaurant rating
        updateRestaurantRating(restaurant.getId());
        
        return mapToResponse(review);
    }
    
    public Page<ReviewResponse> getRestaurantReviews(Long restaurantId, Pageable pageable) {
        return reviewRepository.findByRestaurantId(restaurantId, pageable)
                .map(this::mapToResponse);
    }
    
    public Page<ReviewResponse> getUserReviews(Pageable pageable) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return reviewRepository.findByUserId(user.getId(), pageable)
                .map(this::mapToResponse);
    }
    
    @Transactional
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!review.getUser().getEmail().equals(currentUserEmail)) {
            throw new RuntimeException("You are not authorized to delete this review");
        }
        
        Long restaurantId = review.getRestaurant().getId();
        reviewRepository.delete(review);
        
        // Update restaurant rating
        updateRestaurantRating(restaurantId);
    }
    
    @Transactional
    protected void updateRestaurantRating(Long restaurantId) {
        Double averageRating = reviewRepository.getAverageRatingByRestaurantId(restaurantId);
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        
        if (averageRating != null) {
            restaurant.setRating(BigDecimal.valueOf(averageRating));
        } else {
            restaurant.setRating(BigDecimal.ZERO);
        }
        
        restaurantRepository.save(restaurant);
    }
    
    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFirstName() + " " + review.getUser().getLastName())
                .restaurantId(review.getRestaurant().getId())
                .restaurantName(review.getRestaurant().getName())
                .rating(review.getRating())
                .comment(review.getComment())
                .verified(review.getVerified())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
