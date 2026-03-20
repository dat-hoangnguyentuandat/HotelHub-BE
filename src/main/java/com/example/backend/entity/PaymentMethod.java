package com.example.backend.entity;

/**
 * Phương thức thanh toán được hỗ trợ.
 */
public enum PaymentMethod {

    /** Thẻ tín dụng / ghi nợ (VISA, MasterCard, JCB, NAPAS) */
    CARD,

    /** Quét mã QR (VietQR / liên ngân hàng) */
    QR,

    /** Ví điện tử (MoMo, ZaloPay, VNPay, ShopeePay) */
    WALLET,

    /** Tiền mặt tại quầy khi check-in */
    CASH
}
