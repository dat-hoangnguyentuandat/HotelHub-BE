package com.example.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body để tạo / cập nhật một chính sách hoàn tiền.
 */
@Getter
@Setter
public class PolicyRequest {

    /** Tên chính sách – VD: "Hủy trước 48h hoàn 100%" */
    @NotBlank(message = "Tên chính sách không được để trống")
    @Size(max = 200)
    private String label;

    /**
     * Số giờ tối thiểu trước check-in để áp dụng chính sách.
     * VD: 48 → hủy ít nhất 48 giờ trước check-in
     */
    @NotNull(message = "Số giờ tối thiểu không được để trống")
    @Min(value = 0, message = "Số giờ phải >= 0")
    private Integer minHours;

    /**
     * Tỷ lệ hoàn tiền (0 – 100).
     */
    @NotNull(message = "Tỷ lệ hoàn tiền không được để trống")
    @Min(value = 0,   message = "Tỷ lệ hoàn phải >= 0")
    @Max(value = 100, message = "Tỷ lệ hoàn phải <= 100")
    private Integer refundRate;

    /** Thứ tự hiển thị */
    private int displayOrder;
}
