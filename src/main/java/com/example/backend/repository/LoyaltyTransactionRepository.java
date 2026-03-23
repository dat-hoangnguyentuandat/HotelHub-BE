package com.example.backend.repository;

import com.example.backend.entity.LoyaltyTransaction;
import com.example.backend.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {

    /** Lấy toàn bộ lịch sử của 1 tài khoản, mới nhất trước */
    Page<LoyaltyTransaction> findByLoyaltyAccountIdOrderByCreatedAtDesc(
            Long loyaltyAccountId, Pageable pageable);

    /** Lọc theo type */
    Page<LoyaltyTransaction> findByLoyaltyAccountIdAndTypeOrderByCreatedAtDesc(
            Long loyaltyAccountId, TransactionType type, Pageable pageable);

    /** Lọc theo khoảng thời gian */
    @Query("""
        SELECT t FROM LoyaltyTransaction t
        WHERE t.loyaltyAccount.id = :accountId
          AND (:type IS NULL OR t.type = :type)
          AND (:from IS NULL OR t.createdAt >= :from)
          AND (:to   IS NULL OR t.createdAt <  :to)
        ORDER BY t.createdAt DESC
        """)
    Page<LoyaltyTransaction> findFiltered(
            @Param("accountId") Long accountId,
            @Param("type")      TransactionType type,
            @Param("from")      LocalDateTime from,
            @Param("to")        LocalDateTime to,
            Pageable pageable
    );

    /** Kiểm tra booking đã được tích điểm chưa */
    boolean existsByBookingId(Long bookingId);

    /** Xoá toàn bộ transaction của 1 loyalty account (dùng khi xoá user) */
    void deleteByLoyaltyAccountId(Long loyaltyAccountId);
}
