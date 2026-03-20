package com.example.backend.dto.response;

import com.example.backend.entity.Payment;
import com.example.backend.entity.PaymentMethod;
import com.example.backend.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO trả về sau mỗi thao tác thanh toán.
 */
@Getter
@Builder
public class PaymentResponse {

    private Long id;
    private Long bookingId;
    private String transactionRef;

    /* ── Số tiền ── */
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal vatAmount;
    private BigDecimal totalAmount;

    /* ── Phương thức & trạng thái ── */
    private PaymentMethod method;
    private String methodLabel;
    private PaymentStatus status;
    private String statusLabel;

    /* ── Thẻ (ẩn danh) ── */
    private String cardLastFour;
    private String cardType;
    private String cardHolder;

    /* ── Ví điện tử ── */
    private String walletProvider;

    /* ── Promo & Loyalty ── */
    private String promoCode;
    private BigDecimal promoDiscountRate;
    private int loyaltyPointsUsed;
    private BigDecimal loyaltyDiscount;
    private int loyaltyPointsEarned;

    /* ── Gateway ── */
    private String gatewayTransactionId;
    private String gatewayResponseCode;
    private String gatewayMessage;

    /* ── Thời gian ── */
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime expiresAt;

    private String note;

    /* ── Builder từ entity ── */
    public static PaymentResponse from(Payment p) {
        return PaymentResponse.builder()
            .id(p.getId())
            .bookingId(p.getBooking().getId())
            .transactionRef(p.getTransactionRef())
            .subtotal(p.getSubtotal())
            .discountAmount(p.getDiscountAmount())
            .vatAmount(p.getVatAmount())
            .totalAmount(p.getTotalAmount())
            .method(p.getMethod())
            .methodLabel(methodLabel(p.getMethod()))
            .status(p.getStatus())
            .statusLabel(statusLabel(p.getStatus()))
            .cardLastFour(p.getCardLastFour())
            .cardType(p.getCardType())
            .cardHolder(p.getCardHolder())
            .walletProvider(p.getWalletProvider())
            .promoCode(p.getPromoCode())
            .promoDiscountRate(p.getPromoDiscountRate())
            .loyaltyPointsUsed(p.getLoyaltyPointsUsed())
            .loyaltyDiscount(p.getLoyaltyDiscount())
            .loyaltyPointsEarned(p.getLoyaltyPointsEarned())
            .gatewayTransactionId(p.getGatewayTransactionId())
            .gatewayResponseCode(p.getGatewayResponseCode())
            .gatewayMessage(p.getGatewayMessage())
            .createdAt(p.getCreatedAt())
            .completedAt(p.getCompletedAt())
            .expiresAt(p.getExpiresAt())
            .note(p.getNote())
            .build();
    }

    private static String methodLabel(PaymentMethod m) {
        if (m == null) return "";
        return switch (m) {
            case CARD   -> "Thẻ ngân hàng";
            case QR     -> "Quét QR";
            case WALLET -> "Ví điện tử";
            case CASH   -> "Tiền mặt";
        };
    }

    private static String statusLabel(PaymentStatus s) {
        if (s == null) return "";
        return switch (s) {
            case PENDING    -> "Chờ xử lý";
            case PROCESSING -> "Đang xử lý";
            case SUCCESS    -> "Thành công";
            case FAILED     -> "Thất bại";
            case REFUNDED   -> "Đã hoàn tiền";
            case CANCELLED  -> "Đã hủy";
        };
    }
}
