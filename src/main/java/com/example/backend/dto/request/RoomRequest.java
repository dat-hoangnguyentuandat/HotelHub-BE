package com.example.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class RoomRequest {

    @NotBlank(message = "Tên phòng không được để trống")
    @Size(max = 100, message = "Tên phòng tối đa 100 ký tự")
    private String roomName;

    @NotBlank(message = "Loại phòng không được để trống")
    @Size(max = 100)
    private String roomType;

    private String description;

    @NotNull(message = "Giá/đêm không được để trống")
    @DecimalMin(value = "0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    @Min(value = 1, message = "Sức chứa tối thiểu 1 người")
    @Max(value = 50, message = "Sức chứa tối đa 50 người")
    private int capacity = 2;

    @Pattern(regexp = "Trống|Đã Đặt|Đang ở|Hết Phòng|Bảo Trì",
             message = "Trạng thái không hợp lệ")
    private String status = "Trống";

    private String schedule;

    private boolean maintenance = false;

    @Min(value = 1) @Max(value = 200)
    private Integer floor;

    private List<String> amenities;

    @Size(max = 500, message = "URL ảnh tối đa 500 ký tự")
    private String imageUrl;
}
