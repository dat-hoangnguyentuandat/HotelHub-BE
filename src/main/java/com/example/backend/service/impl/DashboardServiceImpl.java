package com.example.backend.service.impl;

import com.example.backend.dto.response.DashboardResponse;
import com.example.backend.dto.response.DashboardResponse.*;
import com.example.backend.entity.Booking;
import com.example.backend.entity.BookingStatus;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.RoomRepository;
import com.example.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final BookingRepository bookingRepository;
    private final RoomRepository    roomRepository;

    /* ── Formatter cho nhãn tháng tiếng Việt ── */
    private static final DateTimeFormatter MONTH_LABEL_FMT =
            DateTimeFormatter.ofPattern("'Tháng' M");

    @Override
    public DashboardResponse getDashboardData() {
        return DashboardResponse.builder()
                .stats(buildStats())
                .monthlyRevenue(buildMonthlyRevenueChart())
                .roomTypeRevenue(buildRoomTypeRevenueChart())
                .recentTransactions(buildRecentTransactions())
                .build();
    }

    /* ══════════════════════════════════════════════════════════
       1. STATS OVERVIEW – 4 thẻ KPI
    ══════════════════════════════════════════════════════════ */
    private StatsOverview buildStats() {

        LocalDate today          = LocalDate.now();
        LocalDate thisMonthStart = today.withDayOfMonth(1);
        LocalDate thisMonthEnd   = thisMonthStart.plusMonths(1); // exclusive

        LocalDate lastMonthStart = thisMonthStart.minusMonths(1);
        LocalDate lastMonthEnd   = thisMonthStart;               // exclusive

        // ── Tổng số phòng ──
        long totalRooms = roomRepository.count();
        if (totalRooms == 0) totalRooms = 1; // tránh chia 0

        // ── Tỷ lệ lấp đầy: số phòng có booking CONFIRMED/CHECKED_IN hôm nay / tổng phòng ──
        long occupiedToday    = bookingRepository.countOccupiedRoomsToday(today);
        double occupancyRate  = (double) occupiedToday / totalRooms * 100;

        // So sánh với hôm cùng tháng trước (ngày đầu tháng trước để ước tính)
        LocalDate sameDayLastMonth = today.minusMonths(1);
        long occupiedLastMonth     = bookingRepository.countOccupiedRoomsToday(sameDayLastMonth);
        double occupancyRateLast   = (double) occupiedLastMonth / totalRooms * 100;
        double occupancyChange     = Math.round((occupancyRate - occupancyRateLast) * 10.0) / 10.0;

        // ── RevPAR = doanh thu tháng này / tổng phòng ──
        BigDecimal revThisMonth = bookingRepository.sumRevenueBetween(thisMonthStart, thisMonthEnd);
        BigDecimal revLastMonth = bookingRepository.sumRevenueBetween(lastMonthStart, lastMonthEnd);

        BigDecimal revPar = revThisMonth.divide(BigDecimal.valueOf(totalRooms), 0, RoundingMode.HALF_UP);
        double revParChange = 0;
        if (revLastMonth.compareTo(BigDecimal.ZERO) > 0) {
            revParChange = revThisMonth.subtract(revLastMonth)
                    .divide(revLastMonth, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        // ── Tổng lượt check-in tháng này vs tháng trước ──
        long checkInsThisMonth = bookingRepository.countCheckedInBetween(thisMonthStart, thisMonthEnd);
        long checkInsLastMonth = bookingRepository.countCheckedInBetween(lastMonthStart, lastMonthEnd);

        // ── Booking đang chờ xác nhận (thời gian thực) ──
        long pendingNow = bookingRepository.countByStatus(BookingStatus.PENDING);

        return StatsOverview.builder()
                .occupancyRate(Math.round(occupancyRate * 10.0) / 10.0)
                .occupancyRateChange(occupancyChange)
                .revPar(revPar)
                .revParChange(revParChange)
                .totalCheckIns(checkInsThisMonth)
                .totalCheckInsChange(checkInsThisMonth - checkInsLastMonth)
                .pendingBookings(pendingNow)
                .pendingBookingsChange(0L) // không có snapshot lịch sử → 0
                .build();
    }

    /* ══════════════════════════════════════════════════════════
       2. BIỂU ĐỒ DOANH THU HÀNG THÁNG (6 tháng gần nhất)
    ══════════════════════════════════════════════════════════ */
    private MonthlyRevenueChart buildMonthlyRevenueChart() {

        List<String>     labels  = new ArrayList<>();
        List<BigDecimal> data    = new ArrayList<>();
        LocalDate        today   = LocalDate.now();
        BigDecimal       current = BigDecimal.ZERO;

        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate  = today.minusMonths(i);
            LocalDate monthStart = monthDate.withDayOfMonth(1);
            LocalDate monthEnd   = monthStart.plusMonths(1); // exclusive

            BigDecimal revenue = bookingRepository.sumRevenueBetween(monthStart, monthEnd);
            labels.add(monthDate.format(MONTH_LABEL_FMT));
            data.add(revenue);

            if (i == 0) current = revenue;
        }

        return MonthlyRevenueChart.builder()
                .labels(labels)
                .data(data)
                .currentMonthTotal(current)
                .build();
    }

    /* ══════════════════════════════════════════════════════════
       3. BIỂU ĐỒ DOANH THU THEO LOẠI PHÒNG (tháng hiện tại)
    ══════════════════════════════════════════════════════════ */
    private RoomTypeRevenueChart buildRoomTypeRevenueChart() {

        LocalDate today      = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd   = monthStart.plusMonths(1); // exclusive

        List<Object[]>   rows   = bookingRepository.revenueByRoomType(monthStart, monthEnd);
        List<String>     labels = new ArrayList<>();
        List<BigDecimal> data   = new ArrayList<>();
        BigDecimal       total  = BigDecimal.ZERO;

        for (Object[] row : rows) {
            labels.add((String) row[0]);
            BigDecimal amount = (BigDecimal) row[1];
            data.add(amount);
            total = total.add(amount);
        }

        return RoomTypeRevenueChart.builder()
                .labels(labels)
                .data(data)
                .total(total)
                .monthLabel(today.format(MONTH_LABEL_FMT))
                .build();
    }

    /* ══════════════════════════════════════════════════════════
       4. GIAO DỊCH GẦN NHẤT (5 booking mới nhất)
    ══════════════════════════════════════════════════════════ */
    private List<RecentTransaction> buildRecentTransactions() {

        List<Booking> bookings = bookingRepository
                .findTop5RecentTransactions(PageRequest.of(0, 5));

        return bookings.stream()
                .map(b -> RecentTransaction.builder()
                        .id(b.getId())
                        .transactionCode(String.format("TXN%05d", b.getId()))
                        .guestName(b.getGuestName())
                        .roomType(b.getRoomType())
                        .checkIn(b.getCheckIn().toString())
                        .checkOut(b.getCheckOut().toString())
                        .totalAmount(b.getTotalAmount())
                        .status(b.getStatus().name())
                        .statusLabel(mapStatusLabel(b.getStatus()))
                        .build())
                .collect(Collectors.toList());
    }

    /* ── Helper: chuyển enum → nhãn tiếng Việt ── */
    private String mapStatusLabel(BookingStatus status) {
        return switch (status) {
            case PENDING     -> "Chờ xác nhận";
            case CONFIRMED   -> "Đã xác nhận";
            case CHECKED_IN  -> "Đã nhận phòng";
            case CHECKED_OUT -> "Đã trả phòng";
            case CANCELLED   -> "Đã huỷ";
        };
    }
}

