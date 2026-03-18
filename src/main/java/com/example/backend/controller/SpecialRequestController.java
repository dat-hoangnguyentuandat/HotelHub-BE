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
@RequestMapping("/api/admin/special-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
public class SpecialRequestController {

    private final SpecialRequestService service;

    /**
     * GET /api/admin/special-requests
     * ?status=PENDING&keyword=check-in&page=0&size=10
     */
    @GetMapping
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
     * Trả về số lượng theo từng trạng thái.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(service.getStats());
    }

    /**
     * GET /api/admin/special-requests/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SpecialRequestResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    /**
     * POST /api/admin/special-requests
     * Tạo mới yêu cầu đặc biệt.
     */
    @PostMapping
    public ResponseEntity<SpecialRequestResponse> create(
            @Valid @RequestBody SpecialRequestRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    /**
     * PATCH /api/admin/special-requests/{id}/status
     * Body: { "status": "APPROVED", "adminNote": "..." }
     */
    @PatchMapping("/{id}/status")
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Đã xoá yêu cầu #" + id));
    }
}
