package com.example.backend.repository;

import com.example.backend.entity.Staff;
import com.example.backend.entity.StaffStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    /* ── Tìm kiếm theo tên (admin) ── */
    Page<Staff> findByFullNameContainingIgnoreCaseOrderByCreatedAtDesc(
            String keyword, Pageable pageable);

    /* ── Lọc theo trạng thái ── */
    Page<Staff> findByStatusOrderByCreatedAtDesc(StaffStatus status, Pageable pageable);

    /* ── Lọc theo trạng thái + tên ── */
    @Query("""
        SELECT s FROM Staff s
        WHERE s.status = :status
          AND LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY s.createdAt DESC
        """)
    Page<Staff> findByStatusAndFullNameContainingIgnoreCase(
            @Param("status")  StaffStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);

    /* ── Lấy tất cả, mới nhất lên đầu ── */
    Page<Staff> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
