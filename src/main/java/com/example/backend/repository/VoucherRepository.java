package com.example.backend.repository;

import com.example.backend.entity.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    /** Danh sách voucher đang active để khách đổi điểm */
    List<Voucher> findByActiveTrueOrderByPointsRequiredAsc();

    /** Danh sách voucher đang active sắp xếp theo value DESC – dùng cho thuật toán gợi ý hoàn tiền */
    List<Voucher> findByActiveTrueOrderByValueDesc();

    /** Tìm theo id và active */
    Optional<Voucher> findByIdAndActiveTrue(Long id);

    /** Tìm theo code */
    Optional<Voucher> findByCode(String code);

    /** Admin: tìm kiếm & lọc */
    @Query("""
        SELECT v FROM Voucher v
        WHERE (:keyword IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:active IS NULL OR v.active = :active)
        ORDER BY v.createdAt DESC
    """)
    Page<Voucher> findByFilters(
            @Param("keyword") String keyword,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
