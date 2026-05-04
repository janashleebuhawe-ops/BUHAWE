package com.ordering.system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Data
public class Labor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String position;
    private Double dailyWage;
    private String contactNumber;
    private boolean active = true;

    // Tracks when the current shift started (set on activation, cleared on deactivation)
    private Instant shiftStartedAt;

    @Column(nullable = false, columnDefinition = "double default 0.0")
    private Double activeHours = 0.0;

    // --- NEXUS CONNECTION ---
    @OneToOne
    @JoinColumn(name = "user_id")
    private User systemAccount;

    public Double getActiveHours() {
        return activeHours != null ? activeHours : 0.0;
    }

    public void setActiveHours(Double activeHours) {
        this.activeHours = activeHours;
    }
}