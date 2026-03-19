package com.example.backend.controller;

import com.example.backend.dto.request.AdditionalServiceRequest;
import com.example.backend.dto.response.AdditionalServiceResponse;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.service.AdditionalServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','HOTEL_OWNER')")
public class AdditionalServiceController {

    private final AdditionalServiceService service;

    /* ═══════════════════════════════════════════════════════════
       PUBLIC – Khách hàng xem danh sách dịch vụ đang bán
       (override PreAuthorize ở class bằng permitAll trong SecurityConfig)
    ═══════════════════════════════════════════════════════════ */

    /**
     * GET /api/services
     * Trả về tất cả dịch vụ ACTIVE (không cần đăng nhập)
     */
    @GetMapping("/api/services")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<AdditionalServiceResponse>> getActiveServices() {
        return ResponseEntity.ok(service.getActiveServices());
    }

    /* ═══════════════════════════════════════════════════════════
       ADMIN – CRUD
    ═══════════════════════════════════════════════════════════ */

    /**
     * GET /api/admin/services
     * Lấy danh sách tất cả dịch vụ (có tìm kiếm, lọc, phân trang)
     * ?status=ACTIVE&category=Gói ưu đãi&keyword=spa&page=0&size=20
     */
    @GetMapping("/api/admin/services")
    public ResponseEntity<?> getAllServices(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        // Nếu không truyền page/size → trả toàn bộ list (cho frontend hiện tại)
        if (page == null && size == null) {
            return ResponseEntity.ok(service.getAllServices());
        }
        PagedResponse<AdditionalServiceResponse> paged =
                service.searchServices(status, category, keyword,
                        page != null ? page : 0,
                        size != null ? size : 20);
        return ResponseEntity.ok(paged);
    }

    /**
     * GET /api/admin/services/{id}
     */
    @GetMapping("/api/admin/services/{id}")
    public ResponseEntity<AdditionalServiceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    /**
     * POST /api/admin/services
     */
    @PostMapping("/api/admin/services")
    public ResponseEntity<AdditionalServiceResponse> create(
            @Valid @RequestBody AdditionalServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    /**
     * PUT /api/admin/services/{id}
     */
    @PutMapping("/api/admin/services/{id}")
    public ResponseEntity<AdditionalServiceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AdditionalServiceRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    /**
     * DELETE /api/admin/services/{id}
     */
    @DeleteMapping("/api/admin/services/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
