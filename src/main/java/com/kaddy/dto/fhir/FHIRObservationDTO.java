package com.kaddy.dto.fhir;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FHIRObservationDTO {
    private Long patientId;
    private String type;
    private String value;
    private String unit;
    private LocalDateTime effectiveDateTime;
    private String status;
}
