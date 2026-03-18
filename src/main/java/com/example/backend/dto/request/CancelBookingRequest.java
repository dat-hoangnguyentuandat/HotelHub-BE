package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Request khi khách hàng (hoặc admin) hủy một booking.
 * Lý do hủy là tùy chọn.
 */
@Getter
@Setter
public class CancelBookingRequest {

    /** Lý do hủy (không bắt buộc) */
    private String reason;
}
