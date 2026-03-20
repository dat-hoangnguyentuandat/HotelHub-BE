package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request xác thực mã khuyến mãi trước khi thanh toán.
 */
@Getter
@Setter
public class PromoValidateRequest {

    @NotBlank(message = "Mã khuyến mãi không được để trống")
    @Size(max = 30)
    private String code;

    /** Booking ID để kiểm tra điều kiện áp dụng (tuỳ chọn) */
    private Long bookingId;
}
