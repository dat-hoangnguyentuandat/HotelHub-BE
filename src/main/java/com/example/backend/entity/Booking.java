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

    /* ── Thông tin hủy phòng & hoàn tiền ── */

    /** Lý do hủy phòng (do khách nhập khi hủy) */
    @Column(columnDefinition = "TEXT")
    private String cancelReason;

    /** Thời điểm hủy phòng */
    private LocalDateTime cancelledAt;

    /** Tỷ lệ hoàn tiền (0-100), tính theo chính sách tại thời điểm hủy */
    @Column(precision = 5, scale = 2)
    private BigDecimal refundRate;

    /** Số tiền thực tế được hoàn */
    @Column(precision = 15, scale = 0)
    private BigDecimal refundAmount;

    /** Trạng thái hoàn tiền */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RefundStatus refundStatus;

    /** Ghi chú của admin khi xử lý hoàn tiền */
    @Column(columnDefinition = "TEXT")
    private String processNote;

    /** Tên chính sách hoàn tiền đã áp dụng – lưu lại để hiển thị */
    @Column(length = 200)
    private String appliedPolicy;

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
