package com.kaddy.controller;

import com.kaddy.dto.PatientAdmissionDTO;
import com.kaddy.service.AdmissionPdfService;
import com.kaddy.service.PatientAdmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admissions")
@RequiredArgsConstructor
public class PatientAdmissionController {

    private final PatientAdmissionService admissionService;
    private final AdmissionPdfService pdfService;

    @PostMapping("/hospitals/{hospitalId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<PatientAdmissionDTO> createAdmission(@PathVariable Long hospitalId,
            @Valid @RequestBody PatientAdmissionDTO admissionDTO, @RequestAttribute("userId") Long userId) {
        return new ResponseEntity<>(admissionService.createAdmission(hospitalId, admissionDTO, userId),
                HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientAdmissionDTO> getAdmissionById(@PathVariable Long id) {
        return ResponseEntity.ok(admissionService.getAdmissionById(id));
    }

    @GetMapping("/number/{admissionNumber}")
    public ResponseEntity<PatientAdmissionDTO> getAdmissionByNumber(@PathVariable String admissionNumber) {
        return ResponseEntity.ok(admissionService.getAdmissionByNumber(admissionNumber));
    }

    @GetMapping("/hospitals/{hospitalId}/current")
    public ResponseEntity<List<PatientAdmissionDTO>> getCurrentAdmissions(@PathVariable Long hospitalId) {
        return ResponseEntity.ok(admissionService.getCurrentAdmissions(hospitalId));
    }

    @GetMapping("/patients/{patientId}/history")
    public ResponseEntity<List<PatientAdmissionDTO>> getPatientAdmissionHistory(@PathVariable Long patientId) {
        return ResponseEntity.ok(admissionService.getPatientAdmissionHistory(patientId));
    }

    @GetMapping("/wards/{wardId}")
    public ResponseEntity<List<PatientAdmissionDTO>> getAdmissionsByWard(@PathVariable Long wardId) {
        return ResponseEntity.ok(admissionService.getAdmissionsByWard(wardId));
    }

    @GetMapping("/doctors/{doctorId}")
    public ResponseEntity<List<PatientAdmissionDTO>> getAdmissionsByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(admissionService.getAdmissionsByDoctor(doctorId));
    }

    @PostMapping("/{id}/discharge")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<PatientAdmissionDTO> dischargePatient(@PathVariable Long id,
            @Valid @RequestBody PatientAdmissionDTO dischargeInfo, @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(admissionService.dischargePatient(id, dischargeInfo, userId));
    }

    @PostMapping("/{id}/transfer-bed/{newBedId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<PatientAdmissionDTO> transferBed(@PathVariable Long id, @PathVariable Long newBedId) {
        return ResponseEntity.ok(admissionService.transferBed(id, newBedId));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<byte[]> generateAdmissionPdf(@PathVariable Long id) throws IOException {
        byte[] pdfBytes = pdfService.generateAdmissionForm(id);

        PatientAdmissionDTO admission = admissionService.getAdmissionById(id);
        String filename = "Admission_" + admission.getAdmissionNumber() + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}/pdf/print")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<byte[]> getAdmissionPdfForPrint(@PathVariable Long id) throws IOException {
        byte[] pdfBytes = pdfService.generateAdmissionForm(id);

        PatientAdmissionDTO admission = admissionService.getAdmissionById(id);
        String filename = "Admission_" + admission.getAdmissionNumber() + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
