package com.example.backend.dto.response;

import com.example.backend.entity.LoyaltyTransaction;
import com.example.backend.entity.TransactionType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Một dòng lịch sử giao dịch điểm thưởng.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyTransactionResponse {

    private Long            id;
    private TransactionType type;
    private int             points;
    private int             balanceAfter;
    private String          description;
    private Long            bookingId;
    private LocalDateTime   createdAt;

    public static LoyaltyTransactionResponse from(LoyaltyTransaction t) {
        return LoyaltyTransactionResponse.builder()
                .id(t.getId())
                .type(t.getType())
                .points(t.getPoints())
                .balanceAfter(t.getBalanceAfter())
                .description(t.getDescription())
                .bookingId(t.getBooking() != null ? t.getBooking().getId() : null)
                .createdAt(t.getCreatedAt())
                .build();
    }
}
