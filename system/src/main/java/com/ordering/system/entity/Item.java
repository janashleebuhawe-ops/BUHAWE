package com.ordering.system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    // NEW: Added description field (supports up to 500 characters)
    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private String category = "General";

    @Column(nullable = false)
    private Double price = 0.0;

    @Column(nullable = false)
    private Integer stock = 0;

    // Keep this for your Cancel/Restore functionality
    @Column(nullable = false)
    private boolean active = true;
}