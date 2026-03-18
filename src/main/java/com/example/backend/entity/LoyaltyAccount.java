package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Tài khoản điểm thưởng – 1 user có đúng 1 LoyaltyAccount.
 * Tự động tạo khi user đăng ký / khi có giao dịch đầu tiên.
 */
@Entity
@Table(name = "loyalty_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Liên kết 1-1 với User */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** Điểm khả dụng hiện tại */
    @Column(nullable = false)
    @Builder.Default
    private int currentPoints = 0;

    /** Tổng điểm tích lũy từ trước đến nay (chỉ cộng, không trừ) */
    @Column(nullable = false)
    @Builder.Default
    private int totalEarnedPoints = 0;

    /** Hạng thành viên (tự động cập nhật theo totalEarnedPoints) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MembershipTier tier = MembershipTier.SILVER;

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

    /** Cộng điểm và cập nhật hạng tự động */
    public void addPoints(int points) {
        this.currentPoints      += points;
        this.totalEarnedPoints  += points;
        this.tier = MembershipTier.fromPoints(this.totalEarnedPoints);
    }

    /** Trừ điểm (đổi thưởng) */
    public void deductPoints(int points) {
        if (points > this.currentPoints) {
            throw new IllegalArgumentException("Không đủ điểm để đổi thưởng");
        }
        this.currentPoints -= points;
    }
}
