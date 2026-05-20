package com.dineout.dto;

import com.dineout.enums.CuisineType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String phoneNumber;
    private String email;
    private CuisineType cuisineType;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Integer capacity;
    private BigDecimal rating;
    private BigDecimal averageCostForTwo;
    private Boolean acceptsReservations;
    private String imageUrl;
    private Long reviewCount;
}
