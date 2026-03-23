package com.example.backend.controller;

import com.example.backend.dto.request.StaffRequest;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.dto.response.StaffResponse;
import com.example.backend.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/staff")
@PreAuthorize("hasAnyRole('ADMIN','HOTEL_OWNER')")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    /* ── GET /api/admin/staff?keyword=&status=&page=0&size=10 ── */
    @GetMapping
    public ResponseEntity<PagedResponse<StaffResponse>> getAll(
            @RequestParam(defaultValue = "")  String keyword,
            @RequestParam(defaultValue = "")  String status,
            @RequestParam(defaultValue = "0") int    page,
            @RequestParam(defaultValue = "10") int   size
    ) {
        return ResponseEntity.ok(staffService.getAllStaff(keyword, status, page, size));
    }

    /* ── GET /api/admin/staff/{id} ── */
    @GetMapping("/{id}")
    public ResponseEntity<StaffResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(staffService.getById(id));
    }

    /* ── POST /api/admin/staff ── */
    @PostMapping
    public ResponseEntity<StaffResponse> create(@Valid @RequestBody StaffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(staffService.createStaff(request));
    }

    /* ── PUT /api/admin/staff/{id} ── */
    @PutMapping("/{id}")
    public ResponseEntity<StaffResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody StaffRequest request
    ) {
        return ResponseEntity.ok(staffService.updateStaff(id, request));
    }

    /* ── DELETE /api/admin/staff/{id} ── */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        staffService.deleteStaff(id);
        return ResponseEntity.noContent().build();
    }
}
