package com.example.backend.dto.response;

import com.example.backend.entity.Room;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class RoomResponse {

    private Long          id;
    private String        roomName;
    private String        roomType;
    private String        description;
    private BigDecimal    price;
    private int           capacity;
    private String        status;
    private String        schedule;
    private boolean       maintenance;
    private Integer       floor;
    private List<String>  amenities;
    private String        imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RoomResponse from(Room r) {
        return RoomResponse.builder()
                .id(r.getId())
                .roomName(r.getRoomName())
                .roomType(r.getRoomType())
                .description(r.getDescription())
                .price(r.getPrice())
                .capacity(r.getCapacity())
                .status(r.getStatus())
                .schedule(r.getSchedule())
                .maintenance(r.isMaintenance())
                .floor(r.getFloor())
                .amenities(r.getAmenitiesList())
                .imageUrl(r.getImageUrl())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
