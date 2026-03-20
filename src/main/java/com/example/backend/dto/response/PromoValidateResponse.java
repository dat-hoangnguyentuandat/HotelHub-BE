package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Kết quả xác thực mã khuyến mãi.
 */
@Getter
@Builder
public class PromoValidateResponse {

    private boolean valid;
    private String code;
    private String label;

    /** Phần trăm giảm (0.00 – 1.00) */
    private BigDecimal discountRate;

    /** Số tiền giảm tính trên subtotal (nếu client gửi kèm bookingId) */
    private BigDecimal discountAmount;

    private String message;
}
