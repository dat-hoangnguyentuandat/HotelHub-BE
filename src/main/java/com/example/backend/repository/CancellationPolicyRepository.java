package com.example.backend.repository;

import com.example.backend.entity.CancellationPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository cho bảng cancellation_policies.
 */
public interface CancellationPolicyRepository extends JpaRepository<CancellationPolicy, Long> {

    /** Lấy tất cả chính sách, sắp xếp theo minHours giảm dần (ưu tiên chính sách chặt nhất) */
    List<CancellationPolicy> findAllByOrderByMinHoursDesc();

    /** Lấy tất cả chính sách, sắp xếp theo displayOrder */
    List<CancellationPolicy> findAllByOrderByDisplayOrderAsc();
}
