package com.kaddy.dto;

import com.kaddy.model.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineOrderPaymentDTO {
    private Long id;
    private String receiptNumber;
    private Long orderId;
    private String orderNumber;
    private Long patientId;
    private String patientName;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private LocalDateTime paymentDate;
    private String transactionId;
    private String cardLastFourDigits;
    private String bankName;
    private String chequeNumber;
    private LocalDateTime chequeDate;
    private String upiId;
    private Boolean isRefunded;
    private BigDecimal refundedAmount;
    private LocalDateTime refundedAt;
    private String refundReason;
    private Long refundedById;
    private String refundedByName;
    private String notes;
    private Long receivedById;
    private String receivedByName;
    private Boolean isVerified;
    private Long verifiedById;
    private String verifiedByName;
    private LocalDateTime verifiedAt;
}
