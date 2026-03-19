package com.example.backend.repository;

import com.example.backend.entity.AdditionalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdditionalServiceRepository extends JpaRepository<AdditionalService, Long> {

    /** Lấy tất cả dịch vụ đang bán (cho trang booking khách) */
    List<AdditionalService> findByStatusOrderByNameAsc(String status);

    /** Lấy tất cả theo thứ tự tên (admin – không phân trang) */
    List<AdditionalService> findAllByOrderByCreatedAtDesc();

    /** Tìm kiếm + lọc có phân trang (admin) */
    @Query("""
        SELECT s FROM AdditionalService s
        WHERE (:status   IS NULL OR s.status   = :status)
          AND (:category IS NULL OR s.category = :category)
          AND (:keyword  IS NULL
               OR LOWER(s.name)        LIKE LOWER(CONCAT('%',:keyword,'%'))
               OR LOWER(s.description) LIKE LOWER(CONCAT('%',:keyword,'%')))
        ORDER BY s.createdAt DESC
        """)
    Page<AdditionalService> search(
        @Param("status")   String status,
        @Param("category") String category,
        @Param("keyword")  String keyword,
        Pageable pageable
    );
}
