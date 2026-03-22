package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Ghi lại mỗi lần khách hàng đổi điểm lấy voucher.
 */
@Entity
@Table(name = "user_vouchers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    /** Số điểm đã trừ */
    @Column(nullable = false)
    private int pointsSpent;

    /** Mã voucher đã nhận (copy từ voucher.code + suffix để unique) */
    @Column(nullable = false, unique = true, length = 80)
    private String redeemedCode;

    /** Trạng thái sử dụng */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserVoucherStatus status = UserVoucherStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime redeemedAt;

    @PrePersist
    protected void onCreate() {
        this.redeemedAt = LocalDateTime.now();
    }
}
