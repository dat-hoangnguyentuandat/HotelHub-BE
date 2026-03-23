package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity lưu thông tin giao dịch thanh toán.
 *
 * <p>Mỗi {@link Booking} có thể có nhiều Payment (do khách thử nhiều lần),
 * nhưng chỉ 1 Payment ở trạng thái {@code SUCCESS} tại một thời điểm.</p>
 */
@Entity
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_payment_booking",     columnList = "booking_id"),
        @Index(name = "idx_payment_status",      columnList = "status"),
        @Index(name = "idx_payment_txn_ref",     columnList = "transaction_ref", unique = true),
        @Index(name = "idx_payment_created_at",  columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ── Liên kết đặt phòng ── */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    /* ── Số tiền ── */
    /** Tiền gốc (chưa giảm giá, chưa VAT) */
    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal subtotal;

    /** Tiền giảm giá (mã khuyến mãi + điểm thưởng) */
    @Builder.Default
    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /** VAT (10% mặc định) */
    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal vatAmount;

    /** Tổng thanh toán = subtotal - discount + vat */
    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal totalAmount;

    /* ── Phương thức & trạng thái ── */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /* ── Thẻ ngân hàng (chỉ lưu thông tin ẩn danh) ── */
    /** 4 chữ số cuối của thẻ (CARD method) */
    @Column(length = 4)
    private String cardLastFour;

    /** Loại thẻ: VISA / MASTERCARD / JCB / NAPAS */
    @Column(length = 20)
    private String cardType;

    /** Tên chủ thẻ */
    @Column(length = 150)
    private String cardHolder;

    /* ── Ví điện tử ── */
    /** Tên ví: momo / zalopay / vnpay / shopee */
    @Column(length = 30)
    private String walletProvider;

    /* ── Mã khuyến mãi ── */
    @Column(length = 30)
    private String promoCode;

    /** Phần trăm giảm từ promo (0.00 – 1.00) */
    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal promoDiscountRate = BigDecimal.ZERO;

    /* ── Điểm thưởng ── */
    /** Số điểm loyalty đã dùng */
    @Builder.Default
    @Column(nullable = false)
    private int loyaltyPointsUsed = 0;

    /** Giá trị quy đổi từ điểm thưởng */
    @Builder.Default
    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal loyaltyDiscount = BigDecimal.ZERO;

    /* ── Điểm tích luỹ từ giao dịch này ── */
    @Builder.Default
    @Column(nullable = false)
    private int loyaltyPointsEarned = 0;

    /* ── Mã giao dịch & gateway ── */
    /** Mã giao dịch nội bộ (HTH-YYYYMMDD-XXXX) */
    @Column(name = "transaction_ref", nullable = false, length = 60, unique = true)
    private String transactionRef;

    /** Mã phản hồi từ cổng thanh toán (gateway) */
    @Column(length = 100)
    private String gatewayTransactionId;

    /** Mã phản hồi từ gateway (00 = thành công, ...) */
    @Column(length = 10)
    private String gatewayResponseCode;

    /** Thông báo từ gateway */
    @Column(length = 255)
    private String gatewayMessage;

    /* ── Thời gian ── */
    /** Thời điểm bắt đầu phiên thanh toán */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Thời điểm hoàn tất (SUCCESS / FAILED) */
    private LocalDateTime completedAt;

    /** Thời hạn phiên thanh toán (15 phút) */
    private LocalDateTime expiresAt;

    /* ── Ghi chú ── */
    @Column(columnDefinition = "TEXT")
    private String note;

    /* ── Lifecycle ── */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.expiresAt == null) {
            this.expiresAt = this.createdAt.plusMinutes(15);
        }
    }

    /* ── Helper methods ── */

    /** Kiểm tra phiên đã hết hạn chưa */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /** Thanh toán có thể xử lý không (chưa hết hạn, đang PENDING/PROCESSING) */
    public boolean isProcessable() {
        return !isExpired()
            && (status == PaymentStatus.PENDING || status == PaymentStatus.PROCESSING);
    }
}
