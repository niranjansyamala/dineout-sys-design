package com.dineout.entity;

import com.dineout.enums.TableStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "restaurant_tables", indexes = {
    @Index(name = "idx_table_restaurant", columnList = "restaurant_id")
})
public class RestaurantTable extends BaseEntity {
    
    @Column(nullable = false)
    private String tableNumber;
    
    @Column(nullable = false)
    private Integer capacity;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TableStatus status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    
    private String location; // e.g., "Window", "Corner", "Outdoor"
}
