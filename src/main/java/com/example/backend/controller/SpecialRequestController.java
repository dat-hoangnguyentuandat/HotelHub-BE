package com.example.backend.controller;

import com.example.backend.dto.request.SpecialRequestRequest;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.dto.response.SpecialRequestResponse;
import com.example.backend.entity.SpecialRequestStatus;
import com.example.backend.service.SpecialRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SpecialRequestController {

    private final SpecialRequestService service;

    /* ══════════════════════════════════════════════════════════
       ADMIN ENDPOINTS – Yêu cầu quyền ADMIN / HOTEL_OWNER
    ══════════════════════════════════════════════════════════ */

    /**
     * GET /api/admin/special-requests
     * ?status=PENDING&keyword=check-in&page=0&size=10
     */
    @GetMapping("/api/admin/special-requests")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<PagedResponse<SpecialRequestResponse>> getAll(
            @RequestParam(required = false) SpecialRequestStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.getAll(status, keyword, page, size));
    }

    /**
     * GET /api/admin/special-requests/stats
     */
    @GetMapping("/api/admin/special-requests/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(service.getStats());
    }

    /**
     * GET /api/admin/special-requests/{id}
     */
    @GetMapping("/api/admin/special-requests/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<SpecialRequestResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    /**
     * POST /api/admin/special-requests
     * Admin tạo yêu cầu trực tiếp từ panel.
     */
    @PostMapping("/api/admin/special-requests")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<SpecialRequestResponse> create(
            @Valid @RequestBody SpecialRequestRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    /**
     * PATCH /api/admin/special-requests/{id}/status
     * Body: { "status": "APPROVED", "adminNote": "..." }
     */
    @PatchMapping("/api/admin/special-requests/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<SpecialRequestResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String statusStr = body.get("status");
        String adminNote = body.get("adminNote");
        return ResponseEntity.ok(service.updateStatus(id, statusStr, adminNote));
    }

    /**
     * DELETE /api/admin/special-requests/{id}
     */
    @DeleteMapping("/api/admin/special-requests/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Đã xoá yêu cầu #" + id));
    }
}

