package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceBookingResponse {

    private Long id;
    private String bookingCode;
    private Long serviceId;
    private String serviceName;
    private Integer quantity;
    private String guestName;
    private String guestPhone;
    private String guestEmail;
    private String note;
    private Long totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private String message;
}
