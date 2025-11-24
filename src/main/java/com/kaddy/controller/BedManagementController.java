package com.kaddy.controller;

import com.kaddy.dto.BedDTO;
import com.kaddy.dto.WardDTO;
import com.kaddy.model.enums.BedStatus;
import com.kaddy.model.enums.BedType;
import com.kaddy.service.BedManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bed-management")
@RequiredArgsConstructor
public class BedManagementController {

    private final BedManagementService bedManagementService;

    @PostMapping("/hospitals/{hospitalId}/wards")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<WardDTO> createWard(@PathVariable Long hospitalId, @Valid @RequestBody WardDTO wardDTO) {
        return new ResponseEntity<>(bedManagementService.createWard(hospitalId, wardDTO), HttpStatus.CREATED);
    }

    @GetMapping("/hospitals/{hospitalId}/wards")
    public ResponseEntity<List<WardDTO>> getWardsByHospital(@PathVariable Long hospitalId) {
        return ResponseEntity.ok(bedManagementService.getWardsByHospital(hospitalId));
    }

    @GetMapping("/wards/{wardId}")
    public ResponseEntity<WardDTO> getWardById(@PathVariable Long wardId) {
        return ResponseEntity.ok(bedManagementService.getWardById(wardId));
    }

    @PutMapping("/wards/{wardId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<WardDTO> updateWard(@PathVariable Long wardId, @Valid @RequestBody WardDTO wardDTO) {
        return ResponseEntity.ok(bedManagementService.updateWard(wardId, wardDTO));
    }

    @PostMapping("/hospitals/{hospitalId}/beds")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<BedDTO> createBed(@PathVariable Long hospitalId, @Valid @RequestBody BedDTO bedDTO) {
        return new ResponseEntity<>(bedManagementService.createBed(hospitalId, bedDTO), HttpStatus.CREATED);
    }

    @GetMapping("/hospitals/{hospitalId}/beds")
    public ResponseEntity<List<BedDTO>> getBedsByHospital(@PathVariable Long hospitalId) {
        return ResponseEntity.ok(bedManagementService.getBedsByHospital(hospitalId));
    }

    @GetMapping("/wards/{wardId}/beds")
    public ResponseEntity<List<BedDTO>> getBedsByWard(@PathVariable Long wardId) {
        return ResponseEntity.ok(bedManagementService.getBedsByWard(wardId));
    }

    @GetMapping("/hospitals/{hospitalId}/beds/available")
    public ResponseEntity<List<BedDTO>> getAvailableBeds(@PathVariable Long hospitalId) {
        return ResponseEntity.ok(bedManagementService.getAvailableBeds(hospitalId));
    }

    @GetMapping("/hospitals/{hospitalId}/beds/available/type/{bedType}")
    public ResponseEntity<List<BedDTO>> getAvailableBedsByType(@PathVariable Long hospitalId,
            @PathVariable BedType bedType) {
        return ResponseEntity.ok(bedManagementService.getAvailableBedsByType(hospitalId, bedType));
    }

    @GetMapping("/wards/{wardId}/beds/available")
    public ResponseEntity<List<BedDTO>> getAvailableBedsByWard(@PathVariable Long wardId) {
        return ResponseEntity.ok(bedManagementService.getAvailableBedsByWard(wardId));
    }

    @PostMapping("/beds/{bedId}/assign/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<BedDTO> assignPatientToBed(@PathVariable Long bedId, @PathVariable Long patientId) {
        return ResponseEntity.ok(bedManagementService.assignPatientToBed(bedId, patientId));
    }

    @PostMapping("/beds/{bedId}/release")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<BedDTO> releaseBed(@PathVariable Long bedId) {
        return ResponseEntity.ok(bedManagementService.releaseBed(bedId));
    }

    @PatchMapping("/beds/{bedId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<BedDTO> updateBedStatus(@PathVariable Long bedId, @RequestParam BedStatus status) {
        return ResponseEntity.ok(bedManagementService.updateBedStatus(bedId, status));
    }

    @GetMapping("/hospitals/{hospitalId}/statistics")
    public ResponseEntity<Map<String, Object>> getBedStatistics(@PathVariable Long hospitalId) {
        return ResponseEntity.ok(bedManagementService.getBedStatistics(hospitalId));
    }

    @GetMapping("/bed-types")
    public ResponseEntity<BedType[]> getBedTypes() {
        return ResponseEntity.ok(BedType.values());
    }

    @GetMapping("/bed-statuses")
    public ResponseEntity<BedStatus[]> getBedStatuses() {
        return ResponseEntity.ok(BedStatus.values());
    }
}
