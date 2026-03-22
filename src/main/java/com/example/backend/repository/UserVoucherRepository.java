package com.example.backend.repository;

import com.example.backend.entity.UserVoucher;
import com.example.backend.entity.UserVoucherStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {

    /** Voucher của một user */
    Page<UserVoucher> findByUserIdOrderByRedeemedAtDesc(Long userId, Pageable pageable);

    /** Đếm theo voucher (kiểm tra giới hạn) */
    long countByVoucherId(Long voucherId);

    /** Lịch sử theo user + status */
    List<UserVoucher> findByUserIdAndStatus(Long userId, UserVoucherStatus status);

    /**
     * Tìm UserVoucher theo mã đã cấp + trạng thái, JOIN FETCH voucher để tránh LazyLoad.
     * Dùng khi validate mã tại trang payment.
     */
    @Query("SELECT uv FROM UserVoucher uv JOIN FETCH uv.voucher " +
           "WHERE UPPER(uv.redeemedCode) = UPPER(:code) AND uv.status = :status")
    Optional<UserVoucher> findByRedeemedCodeIgnoreCaseAndStatus(
            @Param("code") String redeemedCode,
            @Param("status") UserVoucherStatus status);
}
