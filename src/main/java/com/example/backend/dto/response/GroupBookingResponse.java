package com.example.backend.dto.response;

import com.example.backend.entity.GroupBookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupBookingResponse {

    private Long id;
    private String groupName;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    private Integer totalRooms;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private GroupBookingStatus status;
    private String note;
    private List<GroupBookingRoomResponse> rooms;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
