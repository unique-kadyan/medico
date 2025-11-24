package com.kaddy.controller;

import com.kaddy.dto.PatientDTO;
import com.kaddy.model.Patient;
import com.kaddy.service.PatientService;
import com.kaddy.service.PatientAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Slf4j
public class PatientController {

    private final PatientService patientService;
    private final PatientAccessService patientAccessService;
    private final ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        log.info("Getting all accessible patients");
        List<Patient> accessiblePatients = patientAccessService.getAccessiblePatients();
        List<PatientDTO> patientDTOs = accessiblePatients.stream().map(p -> modelMapper.map(p, PatientDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(patientDTOs);
    }

    @GetMapping("/active")
    public ResponseEntity<List<PatientDTO>> getAllActivePatients() {
        List<Patient> accessiblePatients = patientAccessService.getAccessiblePatients();
        List<PatientDTO> activeDTOs = accessiblePatients.stream().filter(p -> p.getActive() != null && p.getActive())
                .map(p -> modelMapper.map(p, PatientDTO.class)).collect(Collectors.toList());
        return ResponseEntity.ok(activeDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable Long id) {
        log.info("Getting patient by ID: {}", id);
        Patient patient = patientAccessService.getAccessiblePatient(id);
        return ResponseEntity.ok(modelMapper.map(patient, PatientDTO.class));
    }

    @GetMapping("/patient-id/{patientId}")
    public ResponseEntity<PatientDTO> getPatientByPatientId(@PathVariable String patientId) {
        return ResponseEntity.ok(patientService.getPatientByPatientId(patientId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PatientDTO>> searchPatientsByName(@RequestParam String name) {
        return ResponseEntity.ok(patientService.searchPatientsByName(name));
    }

    @PostMapping
    public ResponseEntity<PatientDTO> createPatient(@Valid @RequestBody PatientDTO patientDTO) {
        PatientDTO createdPatient = patientService.createPatient(patientDTO);
        return new ResponseEntity<>(createdPatient, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientDTO> updatePatient(@PathVariable Long id, @Valid @RequestBody PatientDTO patientDTO) {
        return ResponseEntity.ok(patientService.updatePatient(id, patientDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}
