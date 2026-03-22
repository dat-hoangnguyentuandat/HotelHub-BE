package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "group_bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String groupName;

    @Column(nullable = false, length = 150)
    private String contactPerson;

    @Column(nullable = false, length = 20)
    private String contactPhone;

    @Column(length = 150)
    private String contactEmail;

    @Column(nullable = false)
    private Integer totalRooms;

    @Column(nullable = false)
    private LocalDate checkIn;

    @Column(nullable = false)
    private LocalDate checkOut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private GroupBookingStatus status = GroupBookingStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "groupBooking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GroupBookingRoom> rooms = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
