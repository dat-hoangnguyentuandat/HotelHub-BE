package com.example.backend.service.impl;

import com.example.backend.dto.response.ReportResponse;
import com.example.backend.entity.Booking;
import com.example.backend.entity.Payment;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public ReportResponse getRevenueReport(
            LocalDate startDate,
            LocalDate endDate,
            String source,
            String paymentStatus,
            int page,
            int size
    ) {
        log.info("[getRevenueReport] startDate={}, endDate={}, source={}, paymentStatus={}, page={}, size={}",
                startDate, endDate, source, paymentStatus, page, size);

        // Tạo pageable
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Lấy danh sách bookings có payment
        Page<Booking> bookingPage = bookingRepository.findAll(pageable);

        // Filter theo điều kiện
        List<Booking> filteredBookings = bookingPage.getContent().stream()
                .filter(booking -> {
                    // Filter theo ngày
                    if (startDate != null && booking.getCreatedAt().toLocalDate().isBefore(startDate)) {
                        return false;
                    }
                    if (endDate != null && booking.getCreatedAt().toLocalDate().isAfter(endDate)) {
                        return false;
                    }
                    // Filter theo source (tạm thời để mặc định là "OTA" hoặc "Trực tiếp")
                    if (source != null && !source.isEmpty()) {
                        // Logic xác định source dựa trên booking
                        String bookingSource = determineSource(booking);
                        if (!bookingSource.equalsIgnoreCase(source)) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // Tính toán summary
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal actualRevenue = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Booking booking : filteredBookings) {
            List<Payment> payments = paymentRepository.findByBookingIdOrderByCreatedAtDesc(booking.getId());
            Payment payment = payments.isEmpty() ? null : payments.get(0);
            if (payment != null) {
                totalRevenue = totalRevenue.add(payment.getSubtotal());
                actualRevenue = actualRevenue.add(payment.getTotalAmount());
                totalExpenses = totalExpenses.add(payment.getDiscountAmount());
            }
        }

        ReportResponse.ReportSummary summary = ReportResponse.ReportSummary.builder()
                .totalRevenue(totalRevenue)
                .actualRevenue(actualRevenue)
                .totalExpenses(totalExpenses)
                .build();

        // Tạo report items
        List<ReportResponse.ReportItem> items = filteredBookings.stream()
                .map(this::mapToReportItem)
                .collect(Collectors.toList());

        return ReportResponse.builder()
                .summary(summary)
                .items(items)
                .totalItems((int) bookingPage.getTotalElements())
                .currentPage(page)
                .totalPages(bookingPage.getTotalPages())
                .build();
    }

    private ReportResponse.ReportItem mapToReportItem(Booking booking) {
        List<Payment> payments = paymentRepository.findByBookingIdOrderByCreatedAtDesc(booking.getId());
        Payment payment = payments.isEmpty() ? null : payments.get(0);

        String bookingCode = "HD-2023-" + String.format("%03d", booking.getId());
        String source = determineSource(booking);
        String paymentStatusText = "Chờ thanh toán";
        
        if (payment != null && payment.getStatus() != null) {
            switch (payment.getStatus()) {
                case SUCCESS:
                    paymentStatusText = "Đã thanh toán";
                    break;
                case PENDING:
                case PROCESSING:
                    paymentStatusText = "Chờ thanh toán";
                    break;
                case FAILED:
                case CANCELLED:
                    paymentStatusText = "Thất bại";
                    break;
            }
        }

        return ReportResponse.ReportItem.builder()
                .bookingCode(bookingCode)
                .bookingDate(booking.getCreatedAt().toLocalDate())
                .guestName(booking.getGuestName())
                .roomNumber(String.valueOf(booking.getRooms()))
                .source(source)
                .totalAmount(payment != null ? payment.getSubtotal() : booking.getTotalAmount())
                .discountAmount(payment != null ? payment.getDiscountAmount() : BigDecimal.ZERO)
                .finalAmount(payment != null ? payment.getTotalAmount() : booking.getTotalAmount())
                .paymentStatus(paymentStatusText)
                .createdBy(booking.getUser() != null ? booking.getUser().getFullName() : "Khách vãng lai")
                .action("Xem chi tiết")
                .build();
    }

    private String determineSource(Booking booking) {
        // Logic xác định nguồn đặt phòng
        // Nếu có user thì là "Trực tiếp", không có user thì là "OTA"
        if (booking.getUser() != null) {
            return "Trực tiếp";
        } else {
            return "OTA";
        }
    }
}
