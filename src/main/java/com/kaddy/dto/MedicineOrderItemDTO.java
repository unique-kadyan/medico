package com.kaddy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineOrderItemDTO {
    private Long id;
    private Long orderId;
    private Long medicationId;
    private String medicationName;
    private String medicationCode;
    private Long prescriptionItemId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private String dosage;
    private String frequency;
    private Integer duration;
    private String instructions;
}
