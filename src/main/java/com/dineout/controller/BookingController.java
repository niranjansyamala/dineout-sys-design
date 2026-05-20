package com.dineout.controller;

import com.dineout.dto.BookingRequest;
import com.dineout.dto.BookingResponse;
import com.dineout.service.BookingService;
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
@RequestMapping("/api/bookings")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Bookings", description = "Booking management APIs")
public class BookingController {
    
    @Autowired
    private BookingService bookingService;
    
    @PostMapping
    @Operation(summary = "Create a new booking", description = "Create a restaurant booking")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID", description = "Retrieve booking details by ID")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }
    
    @GetMapping("/reference/{reference}")
    @Operation(summary = "Get booking by reference", description = "Retrieve booking details by reference number")
    public ResponseEntity<BookingResponse> getBookingByReference(@PathVariable String reference) {
        return ResponseEntity.ok(bookingService.getBookingByReference(reference));
    }
    
    @GetMapping("/my-bookings")
    @Operation(summary = "Get my bookings", description = "Retrieve all bookings for current user")
    public ResponseEntity<Page<BookingResponse>> getMyBookings(Pageable pageable) {
        return ResponseEntity.ok(bookingService.getMyBookings(pageable));
    }
    
    @PutMapping("/{id}/confirm")
    @Operation(summary = "Confirm booking", description = "Confirm a pending booking")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.confirmBooking(id));
    }
    
    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel booking", description = "Cancel a booking")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, reason));
    }
}
