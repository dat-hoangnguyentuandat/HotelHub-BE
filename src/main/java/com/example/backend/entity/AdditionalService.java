package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "additional_services")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdditionalService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên dịch vụ, bắt buộc */
    @Column(nullable = false, length = 150)
    private String name;

    /** Phân loại: Gói ưu đãi | Vé tham quan | Dịch vụ khác | Ẩm thực | Spa & Làm đẹp | Vận chuyển */
    @Column(nullable = false, length = 100)
    private String category;

    /** Đơn giá */
    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal price;

    /** Đơn vị tính: người / suất / lần … (tuỳ chọn) */
    @Column(length = 50)
    private String unit;

    /** Mô tả chi tiết */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** URL ảnh (path tương đối: /uploads/services/xxx.jpg) */
    @Column(length = 500)
    private String imageUrl;

    /** Trạng thái: ACTIVE | INACTIVE */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

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
