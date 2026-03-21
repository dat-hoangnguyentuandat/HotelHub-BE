package com.example.backend.dto.request;

import com.example.backend.entity.ReviewStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Admin duyệt / từ chối một review.
 */
@Getter
@Setter
public class UpdateReviewStatusRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private ReviewStatus status;
}
