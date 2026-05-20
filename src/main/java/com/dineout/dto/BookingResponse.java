package com.dineout.dto;

import com.dineout.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private Long restaurantId;
    private String restaurantName;
    private LocalDate bookingDate;
    private LocalTime bookingTime;
    private Integer numberOfGuests;
    private BookingStatus status;
    private String specialRequests;
    private String bookingReference;
}
