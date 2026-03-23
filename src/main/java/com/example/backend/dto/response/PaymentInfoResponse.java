package com.example.backend.dto.response;

import com.example.backend.entity.Payment;
import com.example.backend.entity.PaymentMethod;
import com.example.backend.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO chi tiết dành cho trang "Thông tin thanh toán" (payment-info).
 *
 * <p>Mở rộng {@link PaymentResponse}, bổ sung đầy đủ thông tin booking
 * (phòng, ngày, số đêm, mã xác nhận, khách...) để hiển thị trực tiếp
 * trên panel chi tiết mà không cần gọi thêm API.</p>
 */
@Getter
@Builder
public class PaymentInfoResponse {

    /* ── Payment core ── */
    private Long    id;
    private String  transactionRef;
    private String  gatewayTransactionId;
    private String  gatewayResponseCode;
    private String  gatewayMessage;

    /* ── Phương thức & trạng thái ── */
    private PaymentMethod method;
    private String        methodLabel;
    private PaymentStatus status;
    private String        statusLabel;

    /* ── Số tiền ── */
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal vatAmount;
    private BigDecimal totalAmount;

    /* ── Promo ── */
    private String     promoCode;
    private BigDecimal promoDiscountRate;
    private BigDecimal promoDiscount;      // = subtotal * promoDiscountRate

    /* ── Loyalty ── */
    private int        loyaltyPointsUsed;
    private BigDecimal loyaltyDiscount;
    private int        loyaltyPointsEarned;

    /* ── Thẻ / Ví ── */
    private String cardLastFour;
    private String cardType;
    private String cardHolder;
    private String walletProvider;

    /* ── Thời gian ── */
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime expiresAt;

    /* ── Ghi chú ── */
    private String note;

    /* ── Booking info (đầy đủ cho panel chi tiết) ── */
    private Long      bookingId;
    private String    confirmationCode;   // Mã xác nhận: "HTH-1001"
    private String    roomType;
    private String    guestName;
    private String    guestPhone;
    private String    guestEmail;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int       nights;
    private int       rooms;
    private int       adults;
    private int       children;
    private BigDecimal pricePerNight;

    /* ──────────────────────────────────────────────────
       Builder từ entity
    ────────────────────────────────────────────────── */
    public static PaymentInfoResponse from(Payment p) {
        var b = p.getBooking();
        int nightsVal = (b != null) ? b.getNights() : 0;

        // Tính promoDiscount tách riêng để UI hiển thị đúng
        BigDecimal promoDiscount = BigDecimal.ZERO;
        if (p.getSubtotal() != null && p.getPromoDiscountRate() != null
                && p.getPromoDiscountRate().compareTo(BigDecimal.ZERO) > 0) {
            promoDiscount = p.getSubtotal()
                    .multiply(p.getPromoDiscountRate())
                    .setScale(0, java.math.RoundingMode.HALF_UP);
        }

        return PaymentInfoResponse.builder()
                // ── payment ──
                .id(p.getId())
                .transactionRef(p.getTransactionRef())
                .gatewayTransactionId(p.getGatewayTransactionId())
                .gatewayResponseCode(p.getGatewayResponseCode())
                .gatewayMessage(p.getGatewayMessage())
                .method(p.getMethod())
                .methodLabel(methodLabel(p.getMethod()))
                .status(p.getStatus())
                .statusLabel(statusLabel(p.getStatus()))
                .subtotal(p.getSubtotal())
                .discountAmount(p.getDiscountAmount())
                .vatAmount(p.getVatAmount())
                .totalAmount(p.getTotalAmount())
                .promoCode(p.getPromoCode())
                .promoDiscountRate(p.getPromoDiscountRate())
                .promoDiscount(promoDiscount)
                .loyaltyPointsUsed(p.getLoyaltyPointsUsed())
                .loyaltyDiscount(p.getLoyaltyDiscount())
                .loyaltyPointsEarned(p.getLoyaltyPointsEarned())
                .cardLastFour(p.getCardLastFour())
                .cardType(p.getCardType())
                .cardHolder(p.getCardHolder())
                .walletProvider(p.getWalletProvider())
                .createdAt(p.getCreatedAt())
                .completedAt(p.getCompletedAt())
                .expiresAt(p.getExpiresAt())
                .note(p.getNote())
                // ── booking ──
                .bookingId(b != null ? b.getId() : null)
                .confirmationCode(b != null ? "HTH-" + b.getId() : null)
                .roomType(b != null ? b.getRoomType() : null)
                .guestName(b != null ? b.getGuestName() : null)
                .guestPhone(b != null ? b.getGuestPhone() : null)
                .guestEmail(b != null ? b.getGuestEmail() : null)
                .checkIn(b != null ? b.getCheckIn() : null)
                .checkOut(b != null ? b.getCheckOut() : null)
                .nights(nightsVal)
                .rooms(b != null ? b.getRooms() : 1)
                .adults(b != null ? b.getAdults() : 1)
                .children(b != null ? b.getChildren() : 0)
                .pricePerNight(b != null ? b.getPricePerNight() : null)
                .build();
    }

    /* ── Label helpers ── */
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
