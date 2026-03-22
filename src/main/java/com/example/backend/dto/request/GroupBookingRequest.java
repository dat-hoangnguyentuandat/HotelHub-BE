package com.example.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupBookingRequest {

    @NotBlank(message = "Tên đoàn không được để trống")
    @Size(max = 200, message = "Tên đoàn không được vượt quá 200 ký tự")
    private String groupName;

    @NotBlank(message = "Người liên hệ không được để trống")
    @Size(max = 150, message = "Tên người liên hệ không được vượt quá 150 ký tự")
    private String contactPerson;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại không hợp lệ")
    private String contactPhone;

    @Email(message = "Email không hợp lệ")
    @Size(max = 150, message = "Email không được vượt quá 150 ký tự")
    private String contactEmail;

    @NotNull(message = "Số lượng phòng không được để trống")
    @Min(value = 1, message = "Số lượng phòng phải lớn hơn 0")
    private Integer totalRooms;

    @NotNull(message = "Ngày check-in không được để trống")
    private LocalDate checkIn;

    @NotNull(message = "Ngày check-out không được để trống")
    private LocalDate checkOut;

    private String note;

    private List<GroupBookingRoomRequest> rooms;
}
