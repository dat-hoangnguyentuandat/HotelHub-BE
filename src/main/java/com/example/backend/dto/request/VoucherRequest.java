package com.example.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class VoucherRequest {

    @NotBlank(message = "Tên voucher không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Số điểm cần đổi không được để trống")
    @Min(value = 1, message = "Số điểm phải lớn hơn 0")
    private Integer pointsRequired;

    @NotNull(message = "Giá trị voucher không được để trống")
    @Min(value = 1000, message = "Giá trị voucher tối thiểu 1.000đ")
    private Long value;

    private String category;

    private Boolean active;

    private Integer maxRedemptions;
}
