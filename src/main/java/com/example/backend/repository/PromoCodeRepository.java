package com.example.backend.repository;

import com.example.backend.entity.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {

    /** Tìm promo code theo mã (không phân biệt hoa thường) */
    Optional<PromoCode> findByCodeIgnoreCaseAndActiveTrue(String code);

    /** Lấy tất cả promo code đang hoạt động */
    List<PromoCode> findByActiveTrueOrderByCreatedAtDesc();
}
