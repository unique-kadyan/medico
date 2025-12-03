package com.kaddy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionItemDTO {
    private Long id;
    private Long prescriptionId;
    private Long medicationId;
    private String medicationName;
    private String medicationCode;
    private Integer quantity;
    private String dosage;
    private String frequency;
    private Integer duration;
    private String instructions;
    private String warnings;
}
