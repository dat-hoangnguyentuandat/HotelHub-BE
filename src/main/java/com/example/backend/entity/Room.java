package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String roomName;

    @Column(nullable = false, length = 100)
    private String roomType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal price;

    @Column(nullable = false)
    @Builder.Default
    private int capacity = 2;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "Trống";   // Trống | Đã Đặt | Đang ở | Hết Phòng | Bảo Trì

    @Column(length = 255)
    private String schedule;

    @Column(nullable = false)
    @Builder.Default
    private boolean maintenance = false;

    @Column
    private Integer floor;

    /** Tiện nghi lưu dạng chuỗi phân cách dấu phẩy */
    @Column(columnDefinition = "TEXT")
    private String amenities;

    /** URL ảnh đại diện của phòng */
    @Column(length = 500)
    private String imageUrl;

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

    /* Helper: convert list ↔ string */
    @Transient
    public List<String> getAmenitiesList() {
        if (amenities == null || amenities.isBlank()) return new ArrayList<>();
        return List.of(amenities.split(","));
    }

    public void setAmenitiesList(List<String> list) {
        this.amenities = (list == null || list.isEmpty()) ? null : String.join(",", list);
    }
}
