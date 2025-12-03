package com.kaddy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDTO {
    private Long id;
    private String prescriptionNumber;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private LocalDate prescriptionDate;
    private LocalDate expiryDate;
    private String diagnosis;
    private String instructions;
    private Boolean dispensed;
    private LocalDate dispensedDate;
    private Long dispensedById;
    private String dispensedByName;
    private Boolean expired;
    private List<PrescriptionItemDTO> items = new ArrayList<>();
}
