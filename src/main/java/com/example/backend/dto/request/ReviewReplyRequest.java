package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Admin gửi phản hồi cho một review.
 */
@Getter
@Setter
public class ReviewReplyRequest {

    @NotBlank(message = "Nội dung phản hồi không được để trống")
    @Size(max = 2000, message = "Phản hồi tối đa 2000 ký tự")
    private String replyText;
}
