package com.kaddy.dto;

import com.kaddy.model.enums.MedicineOrderStatus;
import com.kaddy.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineOrderDTO {
    private Long id;
    private String orderNumber;
    private Long patientId;
    private String patientName;
    private Long prescriptionId;
    private String prescriptionNumber;
    private LocalDateTime orderDate;
    private MedicineOrderStatus status;
    private PaymentStatus paymentStatus;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String deliveryAddress;
    private String contactPhone;
    private String notes;
    private Long processedById;
    private String processedByName;
    private LocalDateTime processedDate;
    private LocalDateTime deliveryDate;
    private List<MedicineOrderItemDTO> items = new ArrayList<>();
}
