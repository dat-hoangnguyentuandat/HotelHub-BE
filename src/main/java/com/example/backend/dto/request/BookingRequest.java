package com.example.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class BookingRequest {

    @NotBlank(message = "Họ tên khách không được để trống")
    @Size(max = 150, message = "Họ tên tối đa 150 ký tự")
    private String guestName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(
        regexp = "^(\\+84|0)[0-9\\s\\-\\.]{7,13}$",
        message = "Số điện thoại không hợp lệ (VD: 0901234567 hoặc +84901234567)"
    )
    private String guestPhone;

    @Email(message = "Email không hợp lệ")
    private String guestEmail;

    @NotBlank(message = "Loại phòng không được để trống")
    private String roomType;

    @NotNull(message = "Giá/đêm không được để trống")
    @DecimalMin(value = "0", inclusive = false, message = "Giá/đêm phải lớn hơn 0")
    private BigDecimal pricePerNight;

    @NotNull(message = "Ngày nhận phòng không được để trống")
    private LocalDate checkIn;

    @NotNull(message = "Ngày trả phòng không được để trống")
    private LocalDate checkOut;

    @Min(value = 1, message = "Số phòng tối thiểu là 1")
    private int rooms = 1;

    @Min(value = 1, message = "Số người lớn tối thiểu là 1")
    private int adults = 1;

    @Min(value = 0, message = "Số trẻ em không được âm")
    private int children = 0;

    private String note;
}
