package com.example.backend.repository;

import com.example.backend.entity.Payment;
import com.example.backend.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /* ── Lookup ── */
    Optional<Payment> findByTransactionRef(String transactionRef);

    List<Payment> findByBookingIdOrderByCreatedAtDesc(Long bookingId);

    /** Lấy payment thành công của booking */
    Optional<Payment> findByBookingIdAndStatus(Long bookingId, PaymentStatus status);

    /* ── Phiên hết hạn cần dọn dẹp ── */
    @Query("""
        SELECT p FROM Payment p
        WHERE p.status IN ('PENDING','PROCESSING')
          AND p.expiresAt < :now
        """)
    List<Payment> findExpiredPending(@Param("now") LocalDateTime now);

    /* ── Admin: tìm kiếm & lọc ── */
    @Query("""
        SELECT p FROM Payment p
        JOIN p.booking b
        WHERE (:status IS NULL OR p.status = :status)
          AND (:method IS NULL OR p.method = :method)
          AND (:keyword IS NULL OR :keyword = ''
               OR LOWER(b.guestName)     LIKE LOWER(CONCAT('%',:keyword,'%'))
               OR LOWER(b.guestPhone)    LIKE LOWER(CONCAT('%',:keyword,'%'))
               OR LOWER(b.guestEmail)    LIKE LOWER(CONCAT('%',:keyword,'%'))
               OR LOWER(p.transactionRef) LIKE LOWER(CONCAT('%',:keyword,'%'))
          )
          AND (:from IS NULL OR CAST(p.createdAt AS date) >= :from)
          AND (:to   IS NULL OR CAST(p.createdAt AS date) <= :to)
        ORDER BY p.createdAt DESC
        """)
    Page<Payment> searchPayments(
        @Param("status")  PaymentStatus status,
        @Param("method")  com.example.backend.entity.PaymentMethod method,
        @Param("keyword") String keyword,
        @Param("from")    LocalDate from,
        @Param("to")      LocalDate to,
        Pageable pageable
    );

    /* ── Dashboard stats ── */
    @Query("""
        SELECT COALESCE(SUM(p.totalAmount), 0) FROM Payment p
        WHERE p.status = 'SUCCESS'
          AND CAST(p.completedAt AS date) >= :from
          AND CAST(p.completedAt AS date) <= :to
        """)
    BigDecimal sumSuccessRevenueBetween(
        @Param("from") LocalDate from,
        @Param("to")   LocalDate to
    );

    long countByStatus(PaymentStatus status);

    @Query("""
        SELECT p.method, COUNT(p), COALESCE(SUM(p.totalAmount), 0)
        FROM Payment p
        WHERE p.status = 'SUCCESS'
          AND CAST(p.completedAt AS date) >= :from
          AND CAST(p.completedAt AS date) <= :to
        GROUP BY p.method
        """)
    List<Object[]> revenueByMethod(
        @Param("from") LocalDate from,
        @Param("to")   LocalDate to
    );

    /* ══════════════════════════════════════════════════════
       PAYMENT-INFO – queries cho trang thông tin cá nhân
    ══════════════════════════════════════════════════════ */

    /**
     * Lấy toàn bộ giao dịch của một user (theo email), sắp xếp mới nhất trước.
     * Dùng cho trang payment-info khi cần filter phía backend.
     */
    @Query("""
        SELECT p FROM Payment p
        JOIN FETCH p.booking b
        WHERE b.guestEmail = :email
           OR (b.user IS NOT NULL AND b.user.email = :email)
        ORDER BY p.createdAt DESC
        """)
    List<Payment> findAllByUserEmailWithBooking(@Param("email") String email);

    /**
     * Lấy giao dịch của user có phân trang + lọc trạng thái, phương thức, từ khoá và khoảng ngày.
     */
    @Query("""
        SELECT p FROM Payment p
        JOIN p.booking b
        WHERE (b.guestEmail = :email OR (b.user IS NOT NULL AND b.user.email = :email))
          AND (:status  IS NULL OR p.status  = :status)
          AND (:method  IS NULL OR p.method  = :method)
          AND (:keyword IS NULL OR :keyword  = ''
               OR LOWER(p.transactionRef) LIKE LOWER(CONCAT('%',:keyword,'%'))
               OR LOWER(b.roomType)       LIKE LOWER(CONCAT('%',:keyword,'%'))
               OR LOWER(p.gatewayTransactionId) LIKE LOWER(CONCAT('%',:keyword,'%'))
          )
          AND (:from IS NULL OR CAST(p.createdAt AS date) >= :from)
          AND (:to   IS NULL OR CAST(p.createdAt AS date) <= :to)
        ORDER BY p.createdAt DESC
        """)
    Page<Payment> findByUserEmailFiltered(
        @Param("email")   String email,
        @Param("status")  PaymentStatus status,
        @Param("method")  com.example.backend.entity.PaymentMethod method,
        @Param("keyword") String keyword,
        @Param("from")    LocalDate from,
        @Param("to")      LocalDate to,
        Pageable pageable
    );

    /**
     * Đếm giao dịch theo trạng thái cho một user.
     */
    @Query("""
        SELECT COUNT(p) FROM Payment p
        JOIN p.booking b
        WHERE (b.guestEmail = :email OR (b.user IS NOT NULL AND b.user.email = :email))
          AND p.status = :status
        """)
    long countByUserEmailAndStatus(
        @Param("email")  String email,
        @Param("status") PaymentStatus status
    );

    /**
     * Tổng tiền đã chi thành công của user.
     */
    @Query("""
        SELECT COALESCE(SUM(p.totalAmount), 0) FROM Payment p
        JOIN p.booking b
        WHERE (b.guestEmail = :email OR (b.user IS NOT NULL AND b.user.email = :email))
          AND p.status = 'SUCCESS'
        """)
    java.math.BigDecimal sumSuccessAmountByUserEmail(@Param("email") String email);

    /**
     * Tổng điểm thưởng đã tích lũy của user.
     */
    @Query("""
        SELECT COALESCE(SUM(p.loyaltyPointsEarned), 0) FROM Payment p
        JOIN p.booking b
        WHERE (b.guestEmail = :email OR (b.user IS NOT NULL AND b.user.email = :email))
          AND p.status = 'SUCCESS'
        """)
    long sumLoyaltyPointsEarnedByUserEmail(@Param("email") String email);
}
