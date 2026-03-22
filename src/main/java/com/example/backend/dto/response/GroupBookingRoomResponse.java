package com.example.backend.dto.response;

import com.example.backend.entity.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupBookingRoomResponse {

    private Long id;
    private String guestName;
    private String roomType;
    private String roomNumber;
    private RoomStatus status;
    private BigDecimal price;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
