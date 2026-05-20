package com.dineout.service;

import com.dineout.dto.BookingRequest;
import com.dineout.dto.BookingResponse;
import com.dineout.entity.Booking;
import com.dineout.entity.Restaurant;
import com.dineout.entity.User;
import com.dineout.enums.BookingStatus;
import com.dineout.repository.BookingRepository;
import com.dineout.repository.RestaurantRepository;
import com.dineout.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private RestaurantRepository restaurantRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        
        if (!restaurant.getAcceptsReservations()) {
            throw new RuntimeException("This restaurant does not accept reservations");
        }
        
        // Check if time is within restaurant hours
        if (request.getBookingTime().isBefore(restaurant.getOpeningTime()) ||
            request.getBookingTime().isAfter(restaurant.getClosingTime())) {
            throw new RuntimeException("Booking time is outside restaurant operating hours");
        }
        
        // Check capacity
        List<Booking> existingBookings = bookingRepository.findActiveBookingsByRestaurantAndDate(
            restaurant.getId(),
            request.getBookingDate(),
            Arrays.asList(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        );
        
        int totalGuests = existingBookings.stream()
                .mapToInt(Booking::getNumberOfGuests)
                .sum();
        
        if (totalGuests + request.getNumberOfGuests() > restaurant.getCapacity()) {
            throw new RuntimeException("Restaurant is fully booked for this date and time");
        }
        
        // Create booking
        Booking booking = Booking.builder()
                .user(user)
                .restaurant(restaurant)
                .bookingDate(request.getBookingDate())
                .bookingTime(request.getBookingTime())
                .numberOfGuests(request.getNumberOfGuests())
                .specialRequests(request.getSpecialRequests())
                .status(BookingStatus.PENDING)
                .bookingReference(generateBookingReference())
                .build();
        
        booking = bookingRepository.save(booking);
        return mapToResponse(booking);
    }
    
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        return mapToResponse(booking);
    }
    
    public BookingResponse getBookingByReference(String reference) {
        Booking booking = bookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new RuntimeException("Booking not found with reference: " + reference));
        return mapToResponse(booking);
    }
    
    public Page<BookingResponse> getMyBookings(Pageable pageable) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return bookingRepository.findByUserId(user.getId(), pageable)
                .map(this::mapToResponse);
    }
    
    @Transactional
    public BookingResponse confirmBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        booking.setStatus(BookingStatus.CONFIRMED);
        booking = bookingRepository.save(booking);
        return mapToResponse(booking);
    }
    
    @Transactional
    public BookingResponse cancelBooking(Long id, String reason) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!booking.getUser().getEmail().equals(currentUserEmail)) {
            throw new RuntimeException("You are not authorized to cancel this booking");
        }
        
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(reason);
        booking = bookingRepository.save(booking);
        return mapToResponse(booking);
    }
    
    private String generateBookingReference() {
        return "BK" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
    
    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .userEmail(booking.getUser().getEmail())
                .restaurantId(booking.getRestaurant().getId())
                .restaurantName(booking.getRestaurant().getName())
                .bookingDate(booking.getBookingDate())
                .bookingTime(booking.getBookingTime())
                .numberOfGuests(booking.getNumberOfGuests())
                .status(booking.getStatus())
                .specialRequests(booking.getSpecialRequests())
                .bookingReference(booking.getBookingReference())
                .build();
    }
}
