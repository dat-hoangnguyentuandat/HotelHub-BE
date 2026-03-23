package com.example.backend.dto.response;

import com.example.backend.entity.LoyaltyAccount;
import com.example.backend.entity.MembershipTier;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Thông tin tài khoản loyalty trả về cho client.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyAccountResponse {

    private Long   id;
    private Long   userId;
    private String userFullName;
    private String userEmail;
    private String avatarUrl;       // Frontend tự resolve, backend trả null nếu chưa có

    private int currentPoints;
    private int totalEarnedPoints;

    private MembershipTier tier;        // SILVER | GOLD | PLATINUM
    private String         tierLabel;   // "Silver Member" / "Gold Member" / "Platinum Member"

    private int    pointsToNextTier;    // 0 nếu đã Platinum
    private String nextTierName;        // Tên hạng tiếp theo, null nếu đã Platinum

    private int    progressPercent;     // 0-100
    private int    nextThreshold;       // Tổng điểm cần đạt

    private List<String> benefits;      // Danh sách quyền lợi theo hạng

    private LocalDateTime memberSince;  // = loyaltyAccount.createdAt

    /* ── Static factory ── */
    public static LoyaltyAccountResponse from(LoyaltyAccount acc) {

        MembershipTier tier           = acc.getTier();
        int            nextThreshold  = tier.nextThreshold();   // -1 nếu Platinum
        int            totalEarned    = acc.getTotalEarnedPoints();

        int  progressPct;
        int  pointsToNext;
        String nextName;

        if (nextThreshold < 0) {
            // Đã ở Platinum
            progressPct  = 100;
            pointsToNext = 0;
            nextName     = null;
        } else {
            int prevThreshold = switch (tier) {
                case SILVER   -> 0;
                case GOLD     -> 500;
                case PLATINUM -> 1000;
            };
            int range    = nextThreshold - prevThreshold;
            int progress = totalEarned   - prevThreshold;
            progressPct  = (int) Math.min(100, Math.round((double) progress / range * 100));
            pointsToNext = Math.max(0, nextThreshold - totalEarned);
            nextName     = switch (tier) {
                case SILVER -> "Gold";
                case GOLD   -> "Platinum";
                default     -> null;
            };
        }

        List<String> benefits = switch (tier) {
            case SILVER   -> List.of("Giảm 5% giá phòng", "Tích điểm mỗi lần đặt");
            case GOLD     -> List.of("Giảm 10% giá phòng", "Check-in sớm", "Ưu tiên hỗ trợ");
            case PLATINUM -> List.of("Giảm 20% giá phòng",
                                     "Check-in sớm & Check-out muộn",
                                     "Phòng nâng hạng miễn phí",
                                     "Quà tặng đặc biệt");
        };

        String tierLabel = switch (tier) {
            case SILVER   -> "Silver Member";
            case GOLD     -> "Gold Member";
            case PLATINUM -> "Platinum Member";
        };

        return LoyaltyAccountResponse.builder()
                .id(acc.getId())
                .userId(acc.getUser().getId())
                .userFullName(acc.getUser().getFullName())
                .userEmail(acc.getUser().getEmail())
                .avatarUrl(null)
                .currentPoints(acc.getCurrentPoints())
                .totalEarnedPoints(totalEarned)
                .tier(tier)
                .tierLabel(tierLabel)
                .pointsToNextTier(pointsToNext)
                .nextTierName(nextName)
                .progressPercent(progressPct)
                .nextThreshold(nextThreshold < 0 ? 1000 : nextThreshold)
                .benefits(benefits)
                .memberSince(acc.getCreatedAt())
                .build();
    }
}
