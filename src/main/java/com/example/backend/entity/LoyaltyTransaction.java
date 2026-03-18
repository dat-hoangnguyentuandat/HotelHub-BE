package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Lịch sử từng giao dịch điểm thưởng (tích điểm / đổi điểm).
 */
@Entity
@Table(name = "loyalty_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tài khoản loyalty chủ sở hữu */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loyalty_account_id", nullable = false)
    private LoyaltyAccount loyaltyAccount;

    /** Booking liên quan (null nếu là thưởng thủ công / đổi thưởng không qua booking) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    /** EARN hoặc REDEEM */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    /** Số điểm của giao dịch này (luôn dương) */
    @Column(nullable = false)
    private int points;

    /** Số dư sau giao dịch */
    @Column(nullable = false)
    private int balanceAfter;

    /** Mô tả ngắn hiển thị trên UI */
    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
