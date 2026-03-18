package com.example.backend.repository;

import com.example.backend.entity.Booking;
import com.example.backend.entity.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Repository cho module Hủy phòng & Hoàn tiền.
 * Truy vấn trên bảng bookings (lọc những booking đã CANCELLED).
 */
public interface CancellationRepository extends JpaRepository<Booking, Long> {

    // ── Danh sách có filter ────────────────────────────────────────────────

    /**
     * Tìm kiếm các yêu cầu hủy phòng với đầy đủ filter:
     * refundStatus, keyword (mã/tên/phone), khoảng thời gian hủy.
     */
    @Query("""
        SELECT b FROM Booking b
        WHERE b.status = 'CANCELLED'
          AND (:refundStatus IS NULL OR b.refundStatus = :refundStatus)
          AND (
               :keyword IS NULL OR :keyword = ''
               OR LOWER(b.guestName)  LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(b.guestPhone) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(b.guestEmail) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(CONCAT('DP', LPAD(CAST(b.id AS string), 5, '0')))
                       LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
          AND (:cancelFrom IS NULL OR b.cancelledAt >= :cancelFrom)
          AND (:cancelTo   IS NULL OR b.cancelledAt <= :cancelTo)
        ORDER BY b.cancelledAt DESC, b.updatedAt DESC
    """)
    Page<Booking> searchCancellations(
            @Param("refundStatus") RefundStatus refundStatus,
            @Param("keyword")      String keyword,
            @Param("cancelFrom")   LocalDateTime cancelFrom,
            @Param("cancelTo")     LocalDateTime cancelTo,
            Pageable pageable
    );

    // ── Thống kê ────────────────────────────────────────────────────────

    /** Đếm theo refundStatus */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CANCELLED' AND b.refundStatus = :refundStatus")
    long countByRefundStatus(@Param("refundStatus") RefundStatus refundStatus);

    /** Tổng yêu cầu hủy */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CANCELLED'")
    long countAllCancellations();

    /** Tổng số tiền đã hoàn (chỉ trạng thái REFUNDED) */
    @Query("""
        SELECT COALESCE(SUM(b.refundAmount), 0)
        FROM Booking b
        WHERE b.status = 'CANCELLED' AND b.refundStatus = 'REFUNDED'
    """)
    BigDecimal sumRefundedAmount();

    // ── Tìm theo ngày hủy ───────────────────────────────────────────────

    @Query("""
        SELECT b FROM Booking b
        WHERE b.status = 'CANCELLED'
          AND (:cancelFrom IS NULL OR b.cancelledAt >= :cancelFrom)
          AND (:cancelTo   IS NULL OR b.cancelledAt <= :cancelTo)
        ORDER BY b.cancelledAt DESC
    """)
    Page<Booking> findByDateRange(
            @Param("cancelFrom") LocalDateTime cancelFrom,
            @Param("cancelTo")   LocalDateTime cancelTo,
            Pageable pageable
    );
}
