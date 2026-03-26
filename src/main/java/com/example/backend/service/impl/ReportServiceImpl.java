package com.example.backend.service.impl;

import com.example.backend.dto.response.ReportResponse;
import com.example.backend.entity.Booking;
import com.example.backend.entity.Payment;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        // Lấy toàn bộ bookings (sắp xếp giảm dần theo ngày tạo)
        List<Booking> allBookings = bookingRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        // Bước 1: Lọc trước khi phân trang
        List<Booking> filteredBookings = allBookings.stream()
                .filter(booking -> {
                    // Filter theo ngày bắt đầu
                    if (startDate != null && booking.getCreatedAt().toLocalDate().isBefore(startDate)) {
                        return false;
                    }
                    // Filter theo ngày kết thúc
                    if (endDate != null && booking.getCreatedAt().toLocalDate().isAfter(endDate)) {
                        return false;
                    }
                    // Filter theo nguồn (source)
                    if (source != null && !source.isEmpty()) {
                        String bookingSource = determineSource(booking);
                        if (!bookingSource.equalsIgnoreCase(source)) {
                            return false;
                        }
                    }
                    // Filter theo trạng thái thanh toán (paymentStatus)
                    if (paymentStatus != null && !paymentStatus.isEmpty()) {
                        List<Payment> payments = paymentRepository.findByBookingIdOrderByCreatedAtDesc(booking.getId());
                        Payment payment = payments.isEmpty() ? null : payments.get(0);
                        String statusText = resolvePaymentStatusText(payment);
                        if (!statusText.equalsIgnoreCase(paymentStatus)) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // Bước 2: Tính tổng sau khi lọc (summary luôn phản ánh đúng bộ lọc)
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal actualRevenue = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Booking booking : filteredBookings) {
            List<Payment> payments = paymentRepository.findByBookingIdOrderByCreatedAtDesc(booking.getId());
            Payment payment = payments.isEmpty() ? null : payments.get(0);
            if (payment != null) {
                totalRevenue = totalRevenue.add(payment.getSubtotal() != null ? payment.getSubtotal() : BigDecimal.ZERO);
                actualRevenue = actualRevenue.add(payment.getTotalAmount() != null ? payment.getTotalAmount() : BigDecimal.ZERO);
                totalExpenses = totalExpenses.add(payment.getDiscountAmount() != null ? payment.getDiscountAmount() : BigDecimal.ZERO);
            }
        }

        ReportResponse.ReportSummary summary = ReportResponse.ReportSummary.builder()
                .totalRevenue(totalRevenue)
                .actualRevenue(actualRevenue)
                .totalExpenses(totalExpenses)
                .build();

        // Bước 3: Phân trang sau khi đã lọc
        int totalItems = filteredBookings.size();
        int totalPages = (size > 0) ? (int) Math.ceil((double) totalItems / size) : 1;
        int fromIndex = Math.min(page * size, totalItems);
        int toIndex = Math.min(fromIndex + size, totalItems);
        List<Booking> pagedBookings = filteredBookings.subList(fromIndex, toIndex);

        // Bước 4: Map sang ReportItem
        List<ReportResponse.ReportItem> items = pagedBookings.stream()
                .map(this::mapToReportItem)
                .collect(Collectors.toList());

        return ReportResponse.builder()
                .summary(summary)
                .items(items)
                .totalItems(totalItems)
                .currentPage(page)
                .totalPages(totalPages)
                .build();
    }

    private ReportResponse.ReportItem mapToReportItem(Booking booking) {
        List<Payment> payments = paymentRepository.findByBookingIdOrderByCreatedAtDesc(booking.getId());
        Payment payment = payments.isEmpty() ? null : payments.get(0);

        String bookingCode = "HD-2023-" + String.format("%03d", booking.getId());
        String source = determineSource(booking);
        String paymentStatusText = resolvePaymentStatusText(payment);

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

    private String resolvePaymentStatusText(Payment payment) {
        if (payment == null || payment.getStatus() == null) {
            return "Chờ thanh toán";
        }
        switch (payment.getStatus()) {
            case SUCCESS:
                return "Đã thanh toán";
            case PENDING:
            case PROCESSING:
                return "Chờ thanh toán";
            case FAILED:
            case CANCELLED:
                return "Thất bại";
            default:
                return "Chờ thanh toán";
        }
    }
}
