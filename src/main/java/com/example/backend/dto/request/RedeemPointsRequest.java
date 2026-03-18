package com.example.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request đổi điểm thưởng (khách hàng tự đổi).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RedeemPointsRequest {

    @NotNull(message = "Số điểm đổi không được để trống")
    @Min(value = 1, message = "Số điểm đổi phải lớn hơn 0")
    private Integer points;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;
}
