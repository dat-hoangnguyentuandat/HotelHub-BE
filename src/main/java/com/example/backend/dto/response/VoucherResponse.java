package com.example.backend.dto.response;

import com.example.backend.entity.Voucher;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VoucherResponse {

    private Long id;
    private String name;
    private String description;
    private int pointsRequired;
    private long value;
    private String code;
    private String category;
    private boolean active;
    private Integer maxRedemptions;
    private int redeemedCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Còn đổi được không (active + còn slot) */
    private boolean available;

    public static VoucherResponse from(Voucher v) {
        boolean available = v.isActive()
                && (v.getMaxRedemptions() == null || v.getRedeemedCount() < v.getMaxRedemptions());

        return VoucherResponse.builder()
                .id(v.getId())
                .name(v.getName())
                .description(v.getDescription())
                .pointsRequired(v.getPointsRequired())
                .value(v.getValue())
                .code(v.getCode())
                .category(v.getCategory())
                .active(v.isActive())
                .maxRedemptions(v.getMaxRedemptions())
                .redeemedCount(v.getRedeemedCount())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .available(available)
                .build();
    }
}
