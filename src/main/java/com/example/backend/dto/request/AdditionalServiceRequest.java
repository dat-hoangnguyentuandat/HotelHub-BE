package com.example.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AdditionalServiceRequest {

    @NotBlank(message = "Tên dịch vụ không được để trống")
    @Size(max = 150, message = "Tên dịch vụ tối đa 150 ký tự")
    private String name;

    @NotBlank(message = "Phân loại không được để trống")
    @Size(max = 100)
    private String category;

    @NotNull(message = "Đơn giá không được để trống")
    @DecimalMin(value = "0", inclusive = true, message = "Giá phải lớn hơn hoặc bằng 0")
    private BigDecimal price;

    @Size(max = 50, message = "Đơn vị tính tối đa 50 ký tự")
    private String unit;

    private String description;

    // Không giới hạn độ dài – chấp nhận cả URL lẫn base64 string
    private String imageUrl;

    @Pattern(regexp = "ACTIVE|INACTIVE", message = "Trạng thái phải là ACTIVE hoặc INACTIVE")
    private String status = "ACTIVE";
}
