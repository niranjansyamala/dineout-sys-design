package com.dineout.repository;

import com.dineout.entity.Booking;
import com.dineout.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingReference(String bookingReference);
    
    Page<Booking> findByUserId(Long userId, Pageable pageable);
    
    Page<Booking> findByRestaurantId(Long restaurantId, Pageable pageable);
    
    List<Booking> findByRestaurantIdAndBookingDateAndStatus(
        Long restaurantId, 
        LocalDate bookingDate, 
        BookingStatus status
    );
    
    @Query("SELECT b FROM Booking b WHERE b.restaurant.id = :restaurantId " +
           "AND b.bookingDate = :date AND b.status IN :statuses")
    List<Booking> findActiveBookingsByRestaurantAndDate(
        @Param("restaurantId") Long restaurantId,
        @Param("date") LocalDate date,
        @Param("statuses") List<BookingStatus> statuses
    );
}
