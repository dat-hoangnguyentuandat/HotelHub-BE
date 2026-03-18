package com.example.backend.repository;

import com.example.backend.entity.SpecialRequest;
import com.example.backend.entity.SpecialRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpecialRequestRepository extends JpaRepository<SpecialRequest, Long> {

    /** Lọc theo trạng thái */
    Page<SpecialRequest> findByStatus(SpecialRequestStatus status, Pageable pageable);

    /**
     * Tìm kiếm full-text theo tên, loại yêu cầu, nội dung
     * – Bỏ ORDER BY khỏi @Query để Pageable.Sort tự xử lý, tránh xung đột
     */
    @Query(value = """
        SELECT sr FROM SpecialRequest sr
        WHERE (:status IS NULL OR sr.status = :status)
          AND (
               LOWER(sr.guestName)   LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(sr.requestType) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(sr.content)     LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
    """,
    countQuery = """
        SELECT COUNT(sr) FROM SpecialRequest sr
        WHERE (:status IS NULL OR sr.status = :status)
          AND (
               LOWER(sr.guestName)   LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(sr.requestType) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(sr.content)     LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
    """)
    Page<SpecialRequest> search(
            @Param("status")  SpecialRequestStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /** Đếm theo trạng thái (dùng cho stats cards) */
    long countByStatus(SpecialRequestStatus status);
}
