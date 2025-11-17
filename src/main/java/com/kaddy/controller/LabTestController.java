package com.kaddy.controller;

import com.kaddy.dto.LabTestDTO;
import com.kaddy.model.LabTest;
import com.kaddy.service.LabTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lab-tests")
@RequiredArgsConstructor
@Tag(name = "Lab Test Management", description = "APIs for managing laboratory tests")
public class LabTestController {

    private final LabTestService labTestService;

    @GetMapping
    @Operation(summary = "Get all lab tests", description = "Retrieve a list of all laboratory tests")
    public ResponseEntity<List<LabTestDTO>> getAllLabTests() {
        return ResponseEntity.ok(labTestService.getAllLabTests());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get lab test by ID", description = "Retrieve a specific laboratory test by its ID")
    public ResponseEntity<LabTestDTO> getLabTestById(@PathVariable Long id) {
        return ResponseEntity.ok(labTestService.getLabTestById(id));
    }

    @GetMapping("/patient/{id}")
    @Operation(summary = "Get lab tests by patient", description = "Retrieve all laboratory tests for a specific patient")
    public ResponseEntity<List<LabTestDTO>> getLabTestsByPatient(@PathVariable Long id) {
        return ResponseEntity.ok(labTestService.getTestsByPatient(id));
    }

    @GetMapping("/doctor/{id}")
    @Operation(summary = "Get lab tests by doctor", description = "Retrieve all laboratory tests ordered by a specific doctor")
    public ResponseEntity<List<LabTestDTO>> getLabTestsByDoctor(@PathVariable Long id) {
        return ResponseEntity.ok(labTestService.getTestsByDoctor(id));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get lab tests by status", description = "Retrieve all laboratory tests with a specific status")
    public ResponseEntity<List<LabTestDTO>> getLabTestsByStatus(@PathVariable LabTest.TestStatus status) {
        return ResponseEntity.ok(labTestService.getTestsByStatus(status));
    }

    @PostMapping
    @Operation(summary = "Order a lab test", description = "Create a new laboratory test order")
    public ResponseEntity<LabTestDTO> orderLabTest(@Valid @RequestBody LabTestDTO labTestDTO) {
        LabTestDTO createdLabTest = labTestService.orderTest(labTestDTO);
        return new ResponseEntity<>(createdLabTest, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update lab test", description = "Update an existing laboratory test")
    public ResponseEntity<LabTestDTO> updateLabTest(
            @PathVariable Long id,
            @Valid @RequestBody LabTestDTO labTestDTO) {
        return ResponseEntity.ok(labTestService.updateLabTest(id, labTestDTO));
    }

    @PutMapping("/{id}/results")
    @Operation(summary = "Upload lab test results", description = "Upload results for a laboratory test and notify patient and doctor")
    public ResponseEntity<LabTestDTO> uploadResults(
            @PathVariable Long id,
            @RequestBody Map<String, String> resultsData) {
        String testResults = resultsData.get("testResults");
        String resultFilePath = resultsData.get("resultFilePath");
        String remarks = resultsData.get("remarks");

        return ResponseEntity.ok(labTestService.uploadResults(id, testResults, resultFilePath, remarks));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete lab test", description = "Cancel a laboratory test")
    public ResponseEntity<Void> deleteLabTest(@PathVariable Long id) {
        labTestService.deleteLabTest(id);
        return ResponseEntity.noContent().build();
    }
}
