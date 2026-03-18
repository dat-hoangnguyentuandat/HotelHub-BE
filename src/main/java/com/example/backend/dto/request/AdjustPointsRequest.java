package com.example.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request cộng điểm thủ công (Admin).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdjustPointsRequest {

    @NotNull(message = "Số điểm không được để trống")
    @Min(value = 1, message = "Số điểm phải lớn hơn 0")
    private Integer points;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;
}
