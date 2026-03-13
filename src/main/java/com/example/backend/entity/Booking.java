package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ── Thông tin khách (cho cả khách vãng lai không đăng nhập) ── */
    @Column(nullable = false, length = 150)
    private String guestName;

    @Column(nullable = false, length = 20)
    private String guestPhone;

    @Column(length = 150)
    private String guestEmail;

    /* ── Thông tin phòng ── */
    @Column(nullable = false, length = 100)
    private String roomType;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal pricePerNight;

    /* ── Thời gian ── */
    @Column(nullable = false)
    private LocalDate checkIn;

    @Column(nullable = false)
    private LocalDate checkOut;

    @Column(nullable = false)
    private int nights;

    /* ── Số lượng ── */
    @Column(nullable = false)
    private int rooms;

    @Column(nullable = false)
    private int adults;

    @Column(nullable = false)
    private int children;

    /* ── Tổng tiền ── */
    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal totalAmount;

    /* ── Trạng thái ── */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    /* ── Ghi chú ── */
    @Column(columnDefinition = "TEXT")
    private String note;

    /* ── Liên kết user (có thể null nếu khách vãng lai) ── */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /* ── Timestamps ── */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt  = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
