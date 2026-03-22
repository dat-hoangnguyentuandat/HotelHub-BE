package com.example.backend.dto.response;

import com.example.backend.entity.UserVoucher;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserVoucherResponse {

    private Long id;
    private Long voucherId;
    private String voucherName;
    private String voucherDescription;
    private long voucherValue;
    private String voucherCategory;
    private int pointsSpent;
    private String redeemedCode;
    private String status;
    private LocalDateTime redeemedAt;

    public static UserVoucherResponse from(UserVoucher uv) {
        return UserVoucherResponse.builder()
                .id(uv.getId())
                .voucherId(uv.getVoucher().getId())
                .voucherName(uv.getVoucher().getName())
                .voucherDescription(uv.getVoucher().getDescription())
                .voucherValue(uv.getVoucher().getValue())
                .voucherCategory(uv.getVoucher().getCategory())
                .pointsSpent(uv.getPointsSpent())
                .redeemedCode(uv.getRedeemedCode())
                .status(uv.getStatus().name())
                .redeemedAt(uv.getRedeemedAt())
                .build();
    }
}
