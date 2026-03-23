package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpecialRequestRequest {

    @NotBlank(message = "Tên khách không được để trống")
    @Size(max = 150)
    private String guestName;

    @Size(max = 20)
    private String guestPhone;

    @NotBlank(message = "Loại yêu cầu không được để trống")
    @Size(max = 100)
    private String requestType;

    @NotBlank(message = "Nội dung yêu cầu không được để trống")
    private String content;

    private Long bookingId;
}
