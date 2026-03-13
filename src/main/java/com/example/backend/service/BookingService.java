package com.example.backend.service;

import com.example.backend.dto.request.BookingRequest;
import com.example.backend.dto.request.UpdateBookingStatusRequest;
import com.example.backend.dto.response.BookingResponse;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.entity.BookingStatus;

public interface BookingService {

    /** Khách tạo booking (có thể chưa đăng nhập → userEmail null) */
    BookingResponse createBooking(BookingRequest request, String userEmail);

    /** Lấy danh sách booking của user đang đăng nhập */
    PagedResponse<BookingResponse> getMyBookings(String userEmail, int page, int size);

    /** Khách huỷ booking của chính mình */
    BookingResponse cancelBooking(Long bookingId, String userEmail);

    /** ── ADMIN ── */
    PagedResponse<BookingResponse> getAllBookings(BookingStatus status, int page, int size);

    BookingResponse updateStatus(Long bookingId, UpdateBookingStatusRequest request);
}
