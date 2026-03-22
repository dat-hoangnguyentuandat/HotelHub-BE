package com.example.backend.dto.request;

import com.example.backend.entity.RoomStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoomStatusRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private RoomStatus status;
}
