package com.kaddy.dto.fhir;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FHIRMedicationRequestDTO {
    private Long patientId;
    private Long medicationId;
    private Long doctorId;
    private String dosage;
    private String frequency;
    private String status;
    private LocalDateTime authoredOn;
}
