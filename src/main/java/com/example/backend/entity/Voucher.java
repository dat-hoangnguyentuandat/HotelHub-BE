package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Voucher đổi điểm – quản trị viên tạo, khách hàng dùng điểm để đổi.
 */
@Entity
@Table(name = "vouchers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên voucher hiển thị (VD: Giảm 50.000đ, Miễn phí bữa sáng) */
    @Column(nullable = false, length = 200)
    private String name;

    /** Mô tả chi tiết quyền lợi */
    @Column(length = 500)
    private String description;

    /** Số điểm cần để đổi voucher này */
    @Column(nullable = false)
    private int pointsRequired;

    /** Giá trị voucher (VNĐ) */
    @Column(nullable = false)
    private long value;

    /** Mã voucher (tự sinh) – truyền cho khách khi đổi thành công */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /** Phân loại voucher */
    @Column(nullable = false, length = 50)
    @Builder.Default
    private String category = "Giảm giá";

    /** Trạng thái: true = đang hiển thị để đổi, false = ẩn */
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /** Số lượng tối đa có thể đổi (null = không giới hạn) */
    @Column
    private Integer maxRedemptions;

    /** Số lần đã được đổi */
    @Column(nullable = false)
    @Builder.Default
    private int redeemedCount = 0;

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
