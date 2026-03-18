package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Lưu cấu hình chính sách hoàn tiền khi hủy phòng.
 * VD: "Hủy trước 48h hoàn 100%", "Hủy trước 24h hoàn 50%", "Hủy trong ngày không hoàn (0%)"
 */
@Entity
@Table(name = "cancellation_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancellationPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên chính sách hiển thị – VD: "Hủy trước 24h hoàn 50%" */
    @Column(nullable = false, length = 200)
    private String label;

    /**
     * Số giờ tối thiểu trước ngày check-in để áp dụng chính sách này.
     * Chính sách có minHours lớn nhất phù hợp sẽ được ưu tiên.
     * VD: minHours=48 → phải hủy trước ít nhất 48 giờ
     */
    @Column(nullable = false)
    private int minHours;

    /**
     * Tỷ lệ hoàn tiền (0 – 100).
     * VD: 100 → hoàn 100%, 50 → hoàn 50%, 0 → không hoàn
     */
    @Column(nullable = false)
    private int refundRate;

    /** Thứ tự ưu tiên hiển thị (nhỏ hơn = hiển thị trước) */
    @Column(nullable = false)
    @Builder.Default
    private int displayOrder = 0;

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
