package com.kaddy.model;

import com.kaddy.model.enums.PaymentMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "medicine_order_payments", indexes = {
        @Index(name = "idx_med_payment_receipt", columnList = "receiptNumber", unique = true),
        @Index(name = "idx_med_payment_order", columnList = "order_id"),
        @Index(name = "idx_med_payment_date", columnList = "paymentDate")
})
public class MedicineOrderPayment extends BaseEntity {

    @Column(unique = true, nullable = false, length = 50)
    private String receiptNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull(message = "Medicine order is required")
    private MedicineOrder order;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime paymentDate;

    @Column(length = 100)
    private String transactionId;

    @Column(length = 4)
    private String cardLastFourDigits;

    @Column(length = 100)
    private String bankName;

    @Column(length = 100)
    private String chequeNumber;

    private LocalDateTime chequeDate;

    @Column(length = 100)
    private String upiId;

    private Boolean isRefunded = false;

    @Column(precision = 12, scale = 2)
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    private LocalDateTime refundedAt;

    @Column(length = 200)
    private String refundReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refunded_by")
    private User refundedBy;

    @Column(length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by")
    private User receivedBy;

    private Boolean isVerified = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    private LocalDateTime verifiedAt;
}
