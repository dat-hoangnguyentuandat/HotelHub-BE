package com.example.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupBookingRoomRequest {

    @NotBlank(message = "Tên khách không được để trống")
    @Size(max = 150, message = "Tên khách không được vượt quá 150 ký tự")
    private String guestName;

    @NotBlank(message = "Loại phòng không được để trống")
    @Size(max = 100, message = "Loại phòng không được vượt quá 100 ký tự")
    private String roomType;

    @Size(max = 50, message = "Số phòng không được vượt quá 50 ký tự")
    private String roomNumber;

    @NotNull(message = "Giá phòng không được để trống")
    @DecimalMin(value = "0", message = "Giá phòng phải lớn hơn hoặc bằng 0")
    private BigDecimal price;

    private String note;
}
