package com.example.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RedeemVoucherRequest {

    @NotNull(message = "ID voucher không được để trống")
    private Long voucherId;
}
