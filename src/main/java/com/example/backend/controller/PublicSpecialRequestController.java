package com.example.backend.controller;

import com.example.backend.dto.request.SpecialRequestRequest;
import com.example.backend.dto.response.SpecialRequestResponse;
import com.example.backend.service.SpecialRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Public endpoint – không yêu cầu đăng nhập.
 * Cho phép khách vãng lai gửi yêu cầu đặc biệt từ giao diện user.
 *
 * POST /api/special-requests/public
 */
@RestController
@RequestMapping("/api/special-requests/public")
@RequiredArgsConstructor
public class PublicSpecialRequestController {

    private final SpecialRequestService service;

    /**
     * POST /api/special-requests/public
     * Body: { guestName, guestPhone?, requestType, content, bookingId? }
     */
    @PostMapping
    public ResponseEntity<?> createPublic(
            @Valid @RequestBody SpecialRequestRequest request
    ) {
        try {
            SpecialRequestResponse response = service.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Không thể gửi yêu cầu"));
        }
    }
}
