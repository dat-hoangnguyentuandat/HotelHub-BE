package com.example.backend.dto.response;

import com.example.backend.entity.AdditionalService;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AdditionalServiceResponse {

    private Long          id;
    private String        name;
    private String        category;
    private BigDecimal    price;
    private String        unit;
    private String        description;
    private String        imageUrl;
    private String        status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdditionalServiceResponse from(AdditionalService s) {
        return AdditionalServiceResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .category(s.getCategory())
                .price(s.getPrice())
                .unit(s.getUnit())
                .description(s.getDescription())
                .imageUrl(s.getImageUrl())
                .status(s.getStatus())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
