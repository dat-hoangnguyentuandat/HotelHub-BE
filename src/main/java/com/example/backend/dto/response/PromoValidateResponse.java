package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Kết quả xác thực mã khuyến mãi hoặc mã voucher đổi điểm.
 */
@Getter
@Builder
public class PromoValidateResponse {

    private boolean valid;
    private String code;
    private String label;

    /** Phần trăm giảm (0.00 – 1.00) – dùng cho PromoCode truyền thống */
    private BigDecimal discountRate;

    /** Số tiền giảm tính trên subtotal (nếu client gửi kèm bookingId) */
    private BigDecimal discountAmount;

    private String message;

    /**
     * true nếu đây là mã voucher đổi điểm (giảm tiền cố định),
     * false nếu là promo code truyền thống (giảm theo %).
     */
    @Builder.Default
    private boolean isVoucher = false;

    /**
     * Giá trị cố định của voucher (VNĐ) – chỉ có khi isVoucher = true.
     * Frontend dùng trường này thay cho discountRate.
     */
    private BigDecimal voucherValue;
}
