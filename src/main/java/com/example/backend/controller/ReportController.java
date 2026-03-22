package com.example.backend.controller;

import com.example.backend.dto.response.ReportResponse;
import com.example.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * GET /api/admin/reports/revenue
     * Lấy báo cáo doanh thu và công nợ chi tiết
     */
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<ReportResponse> getRevenueReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ReportResponse report = reportService.getRevenueReport(
                startDate, endDate, source, paymentStatus, page, size
        );
        return ResponseEntity.ok(report);
    }
}
