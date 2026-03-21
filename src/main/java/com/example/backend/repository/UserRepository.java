package com.example.backend.repository;

import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /* ── Tìm kiếm & lọc ──────────────────────────────────────────────────── */

    /** Tìm theo role */
    List<User> findByRole(Role role);

    /** Tìm tất cả, sắp xếp mới nhất lên đầu */
    List<User> findAllByOrderByCreatedAtDesc();

    /**
     * Tìm kiếm full-text (tên HOẶC email) kết hợp lọc role.
     * Nếu role = null → tìm tất cả vai trò.
     */
    @Query("""
        SELECT u FROM User u
        WHERE (:role IS NULL OR u.role = :role)
          AND (
                LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        ORDER BY u.createdAt DESC
    """)
    List<User> searchUsers(@Param("keyword") String keyword,
                           @Param("role")    Role   role);

    /* ── Thống kê ─────────────────────────────────────────────────────────── */

    long countByRole(Role role);

    long countByCreatedAtAfter(LocalDateTime since);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :start AND u.createdAt < :end")
    long countByCreatedAtBetween(@Param("start") LocalDateTime start,
                                 @Param("end")   LocalDateTime end);
}
