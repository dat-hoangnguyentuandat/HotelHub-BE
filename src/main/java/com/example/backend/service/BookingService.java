package com.example.backend.service;

import com.example.backend.dto.request.BookingRequest;
import com.example.backend.dto.request.UpdateBookingStatusRequest;
import com.example.backend.dto.response.BookingResponse;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.entity.BookingStatus;

import java.time.LocalDate;

public interface BookingService {

    /** Khách tạo booking (có thể chưa đăng nhập → userEmail null) */
    BookingResponse createBooking(BookingRequest request, String userEmail);

    /** Lấy danh sách booking của user đang đăng nhập */
    PagedResponse<BookingResponse> getMyBookings(String userEmail, int page, int size);

    /** Khách huỷ booking của chính mình */
    BookingResponse cancelBooking(Long bookingId, String userEmail);

    /** ── ADMIN ── */

    /** Lấy tất cả booking có phân trang, lọc theo status */
    PagedResponse<BookingResponse> getAllBookings(BookingStatus status, int page, int size);

    /**
     * Tìm kiếm nâng cao: keyword + status + khoảng ngày checkIn.
     */
    PagedResponse<BookingResponse> searchBookings(
        BookingStatus status,
        String        keyword,
        LocalDate     checkInFrom,
        LocalDate     checkInTo,
        int page,
        int size
    );

    /** Lấy chi tiết một booking */
    BookingResponse getBookingById(Long bookingId);

    /** Cập nhật trạng thái booking */
    BookingResponse updateStatus(Long bookingId, UpdateBookingStatusRequest request);

    /** Admin xoá cứng booking (chỉ cho phép khi đã CANCELLED) */
    void deleteBooking(Long bookingId);
}
