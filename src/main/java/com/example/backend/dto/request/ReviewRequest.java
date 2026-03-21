package com.example.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Khách gửi đánh giá sau khi check-out.
 */
@Getter
@Setter
public class ReviewRequest {

    @NotNull(message = "Mã booking không được để trống")
    private Long bookingId;

    @NotNull(message = "Điểm đánh giá không được để trống")
    @Min(value = 1, message = "Điểm tối thiểu là 1")
    @Max(value = 5, message = "Điểm tối đa là 5")
    private Integer rating;

    @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự")
    private String title;

    @Size(max = 3000, message = "Nội dung nhận xét tối đa 3000 ký tự")
    private String comment;

    /* ── Điểm chi tiết (tùy chọn, 1-5) ── */
    @Min(1) @Max(5) private Integer roomRating;
    @Min(1) @Max(5) private Integer serviceRating;
    @Min(1) @Max(5) private Integer locationRating;
    @Min(1) @Max(5) private Integer cleanlinessRating;
    @Min(1) @Max(5) private Integer amenitiesRating;
    @Min(1) @Max(5) private Integer valueRating;
}
