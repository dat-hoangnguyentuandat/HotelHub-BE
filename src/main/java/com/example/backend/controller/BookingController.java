package com.example.backend.controller;

import com.example.backend.dto.request.BookingRequest;
import com.example.backend.dto.request.UpdateBookingStatusRequest;
import com.example.backend.dto.response.BookingResponse;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.entity.BookingStatus;
import com.example.backend.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /* ═══════════════════════════════════════════════════════════════
       PUBLIC / GUEST ENDPOINTS
    ═══════════════════════════════════════════════════════════════ */

    /**
     * POST /api/bookings
     * Tạo booking – khách vãng lai OK (không cần token).
     */
    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        String userEmail = (principal != null) ? principal.getUsername() : null;
        BookingResponse response = bookingService.createBooking(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/bookings/my?page=0&size=10
     * Danh sách booking của tôi (cần đăng nhập).
     */
    @GetMapping("/bookings/my")
    public ResponseEntity<PagedResponse<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<BookingResponse> data =
                bookingService.getMyBookings(principal.getUsername(), page, size);
        return ResponseEntity.ok(data);
    }

    /**
     * DELETE /api/bookings/{id}/cancel
     * Huỷ booking của tôi (cần đăng nhập, phải là chủ booking).
     */
    @DeleteMapping("/bookings/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelMyBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        BookingResponse response = bookingService.cancelBooking(id, principal.getUsername());
        return ResponseEntity.ok(response);
    }

    /* ═══════════════════════════════════════════════════════════════
       ADMIN ENDPOINTS  (ADMIN hoặc HOTEL_OWNER)
    ═══════════════════════════════════════════════════════════════ */

    /**
     * GET /api/admin/bookings?status=PENDING&keyword=nguyen&checkInFrom=2026-03-01&checkInTo=2026-03-31&page=0&size=20
     *
     * Nếu có keyword/checkInFrom/checkInTo thì dùng searchBookings (full-text),
     * ngược lại dùng getAllBookings (đơn giản, tối ưu hơn).
     */
    @GetMapping("/admin/bookings")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<PagedResponse<BookingResponse>> getAdminBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) String        keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInTo,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        boolean hasSearch = (keyword != null && !keyword.isBlank())
                         || checkInFrom != null
                         || checkInTo   != null;

        PagedResponse<BookingResponse> result = hasSearch
                ? bookingService.searchBookings(status, keyword, checkInFrom, checkInTo, page, size)
                : bookingService.getAllBookings(status, page, size);

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/admin/bookings/{id}
     * Chi tiết một booking.
     */
    @GetMapping("/admin/bookings/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    /**
     * PATCH /api/admin/bookings/{id}/status
     * Cập nhật trạng thái booking.
     * Body: { "status": "CONFIRMED" }
     */
    @PatchMapping("/admin/bookings/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<BookingResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookingStatusRequest request
    ) {
        return ResponseEntity.ok(bookingService.updateStatus(id, request));
    }

    /**
     * DELETE /api/admin/bookings/{id}
     * Xoá cứng booking (chỉ cho phép khi đã CANCELLED).
     */
    @DeleteMapping("/admin/bookings/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<Map<String, String>> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok(Map.of(
            "message", "Đã xoá booking #" + id + " thành công"
        ));
    }
}
