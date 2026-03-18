package com.example.backend.entity;

/**
 * Enum hạng thành viên dựa trên tổng điểm tích lũy.
 *  Silver   : 0   – 499
 *  Gold     : 500 – 999
 *  Platinum : 1000+
 */
public enum MembershipTier {
    SILVER, GOLD, PLATINUM;

    /** Tính hạng dựa trên điểm tích lũy hiện tại. */
    public static MembershipTier fromPoints(int points) {
        if (points >= 1000) return PLATINUM;
        if (points >= 500)  return GOLD;
        return SILVER;
    }

    /** Số điểm cần để đạt hạng tiếp theo (-1 nếu đã Platinum). */
    public int nextThreshold() {
        return switch (this) {
            case SILVER   -> 500;
            case GOLD     -> 1000;
            case PLATINUM -> -1;
        };
    }
}
