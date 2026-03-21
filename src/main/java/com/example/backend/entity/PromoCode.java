package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Mã khuyến mãi – lưu trữ trong DB thay vì hard-code trong service.
 */
@Entity
@Table(name = "promo_codes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã code (VD: HOTEL10, SUMMER20) – duy nhất, không phân biệt hoa thường */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /** Tên hiển thị (VD: Giảm 10%, Giảm 20% Hè 2026) */
    @Column(nullable = false, length = 200)
    private String label;

    /**
     * Tỉ lệ giảm giá (0.10 = 10%, 0.20 = 20%, ...).
     * Lưu dạng thập phân để tính toán chính xác.
     */
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal discountRate;

    /** Trạng thái: true = đang hoạt động, false = đã tắt */
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

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
