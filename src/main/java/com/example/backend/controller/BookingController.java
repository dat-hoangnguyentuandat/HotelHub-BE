package com.example.backend.controller;

import com.example.backend.dto.request.BookingRequest;
import com.example.backend.dto.request.UpdateBookingStatusRequest;
import com.example.backend.dto.response.BookingResponse;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.entity.BookingStatus;
import com.example.backend.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /* ─────────────────────────────────────────────────────────────
       POST /api/bookings  — Tạo booking (public, khách vãng lai OK)
    _____________________________________________________________ */
    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        String userEmail = (principal != null) ? principal.getUsername() : null;
        BookingResponse response = bookingService.createBooking(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /* ─────────────────────────────────────────────────────────────
       GET /api/bookings/my  — Booking của tôi (cần đăng nhập)
    _____________________________________________________________ */
    @GetMapping("/bookings/my")
    public ResponseEntity<PagedResponse<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<BookingResponse> data =
                bookingService.getMyBookings(principal.getUsername(), page, size);
        return ResponseEntity.ok(data);
    }

    /* ─────────────────────────────────────────────────────────────
       DELETE /api/bookings/{id}/cancel  — Huỷ booking của tôi
    _____________________________________________________________ */
    @DeleteMapping("/bookings/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        BookingResponse response = bookingService.cancelBooking(id, principal.getUsername());
        return ResponseEntity.ok(response);
    }

    /* ═════════════════════════════════════════════════════════════
       ADMIN ENDPOINTS
    ═════════════════════════════════════════════════════════════ */

    /* GET /api/admin/bookings?status=PENDING&page=0&size=20 */
    @GetMapping("/admin/bookings")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<PagedResponse<BookingResponse>> getAllBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(bookingService.getAllBookings(status, page, size));
    }

    /* PATCH /api/admin/bookings/{id}/status */
    @PatchMapping("/admin/bookings/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<BookingResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookingStatusRequest request
    ) {
        return ResponseEntity.ok(bookingService.updateStatus(id, request));
    }
}
