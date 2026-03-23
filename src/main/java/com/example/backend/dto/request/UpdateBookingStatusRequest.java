package com.example.backend.dto.request;

import com.example.backend.entity.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBookingStatusRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private BookingStatus status;
}
