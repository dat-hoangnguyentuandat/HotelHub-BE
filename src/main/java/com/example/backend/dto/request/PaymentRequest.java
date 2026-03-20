package com.example.backend.dto.request;

import com.example.backend.entity.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Request body gửi lên khi khởi tạo hoặc xác nhận thanh toán.
 */
@Getter
@Setter
public class PaymentRequest {

    /* ── Bắt buộc ── */

    @NotNull(message = "Mã đặt phòng không được để trống")
    private Long bookingId;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod method;

    /* ── Thông tin thẻ (bắt buộc khi method = CARD) ── */

    /**
     * 4 chữ số cuối thẻ – frontend chỉ gửi phần ẩn danh,
     * không gửi số thẻ đầy đủ lên server.
     */
    @Size(min = 4, max = 4, message = "Chỉ gửi 4 chữ số cuối của thẻ")
    private String cardLastFour;

    /** Loại thẻ: VISA | MASTERCARD | JCB | NAPAS */
    @Size(max = 20)
    private String cardType;

    /** Tên chủ thẻ (in hoa) */
    @Size(max = 150)
    private String cardHolder;

    /* ── Ví điện tử (bắt buộc khi method = WALLET) ── */

    /** Nhà cung cấp ví: momo | zalopay | vnpay | shopee */
    @Size(max = 30)
    private String walletProvider;

    /* ── Khuyến mãi & điểm thưởng ── */

    /** Mã promo đã xác thực (tuỳ chọn) */
    @Size(max = 30)
    private String promoCode;

    /**
     * Phần trăm giảm từ promo (0.00 – 1.00).
     * Frontend tính, backend kiểm tra lại.
     */
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "1.00")
    private BigDecimal promoDiscountRate;

    /** Số điểm loyalty muốn dùng (>= 0) */
    @Min(0)
    private int loyaltyPointsUsed;

    /* ── Ghi chú ── */
    private String note;
}
