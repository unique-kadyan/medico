package com.kaddy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMedicineOrderRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    private Long prescriptionId;

    private String deliveryAddress;

    private String contactPhone;

    private String notes;

    private BigDecimal discountAmount;

    @NotNull(message = "Order items are required")
    private List<CreateMedicineOrderItemRequest> items = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateMedicineOrderItemRequest {
        @NotNull(message = "Medication ID is required")
        private Long medicationId;

        private Long prescriptionItemId;

        @NotNull(message = "Quantity is required")
        private Integer quantity;

        private String dosage;
        private String frequency;
        private Integer duration;
        private String instructions;
    }
}
