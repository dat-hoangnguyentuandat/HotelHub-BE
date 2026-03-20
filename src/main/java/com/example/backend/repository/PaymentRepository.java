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
}
