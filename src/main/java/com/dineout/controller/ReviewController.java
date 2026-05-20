package com.dineout.controller;

import com.dineout.dto.ReviewRequest;
import com.dineout.dto.ReviewResponse;
import com.dineout.service.ReviewService;
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
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "Review management APIs")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @PostMapping
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create a review", description = "Create a review for a restaurant")
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(request));
    }
    
    @GetMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Get restaurant reviews", description = "Get all reviews for a restaurant")
    public ResponseEntity<Page<ReviewResponse>> getRestaurantReviews(
            @PathVariable Long restaurantId,
            Pageable pageable) {
        return ResponseEntity.ok(reviewService.getRestaurantReviews(restaurantId, pageable));
    }
    
    @GetMapping("/my-reviews")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get my reviews", description = "Get all reviews by current user")
    public ResponseEntity<Page<ReviewResponse>> getMyReviews(Pageable pageable) {
        return ResponseEntity.ok(reviewService.getUserReviews(pageable));
    }
    
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete review", description = "Delete a review")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
