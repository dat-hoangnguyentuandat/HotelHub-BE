package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ── Thông tin cơ bản ── */
    @Column(nullable = false, length = 150)
    private String fullName;

    @Column(length = 100)
    private String role;           // Lễ tân | Buồng phòng | Quản lý | Bếp | Bảo vệ

    @Column(length = 20)
    private String phone;

    @Column(length = 150)
    private String email;

    /* ── Ca làm việc ── */
    @Column(length = 200)
    private String shift;

    /* ── Trạng thái ── */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StaffStatus status = StaffStatus.WORKING;

    /* ── Ghi chú ── */
    @Column(columnDefinition = "TEXT")
    private String note;

    /* ── Ảnh đại diện (Base64 hoặc URL, lưu dạng TEXT) ── */
    @Column(columnDefinition = "MEDIUMTEXT")
    private String avatar;

    /* ── Timestamps ── */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt  = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
