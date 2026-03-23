package com.example.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceBookingRequest {

    @NotNull(message = "Service ID không được để trống")
    private Long serviceId;

    @NotBlank(message = "Tên dịch vụ không được để trống")
    private String serviceName;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    @NotBlank(message = "Họ tên không được để trống")
    private String guestName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String guestPhone;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String guestEmail;

    private String note;

    @NotNull(message = "Tổng tiền không được để trống")
    private Long totalAmount;
}
