package com.kaddy.model;

import com.kaddy.model.enums.MedicineOrderStatus;
import com.kaddy.model.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "medicine_orders")
public class MedicineOrder extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @NotNull(message = "Patient is required")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id")
    private Prescription prescription;

    @NotNull(message = "Order date is required")
    @Column(nullable = false)
    private LocalDateTime orderDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MedicineOrderStatus status = MedicineOrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal finalAmount = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column
    private String contactPhone;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private User processedBy;

    @Column
    private LocalDateTime processedDate;

    @Column
    private LocalDateTime deliveryDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MedicineOrderItem> items = new ArrayList<>();

    public void calculateTotals() {
        this.totalAmount = items.stream()
                .map(MedicineOrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.finalAmount = this.totalAmount
                .subtract(this.discountAmount != null ? this.discountAmount : BigDecimal.ZERO);
    }

    public void addItem(MedicineOrderItem item) {
        items.add(item);
        item.setOrder(this);
        calculateTotals();
    }

    public void removeItem(MedicineOrderItem item) {
        items.remove(item);
        item.setOrder(null);
        calculateTotals();
    }
}
