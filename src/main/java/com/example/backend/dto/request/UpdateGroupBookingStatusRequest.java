package com.example.backend.dto.request;

import com.example.backend.entity.GroupBookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGroupBookingStatusRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private GroupBookingStatus status;
}
