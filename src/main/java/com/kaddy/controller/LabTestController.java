package com.kaddy.controller;

import com.kaddy.dto.LabTestDTO;
import com.kaddy.model.LabTest;
import com.kaddy.service.LabTestService;
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
public class LabTestController {

    private final LabTestService labTestService;

    @GetMapping
    public ResponseEntity<List<LabTestDTO>> getAllLabTests() {
        return ResponseEntity.ok(labTestService.getAllLabTests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LabTestDTO> getLabTestById(@PathVariable Long id) {
        return ResponseEntity.ok(labTestService.getLabTestById(id));
    }

    @GetMapping("/patient/{id}")
    public ResponseEntity<List<LabTestDTO>> getLabTestsByPatient(@PathVariable Long id) {
        return ResponseEntity.ok(labTestService.getTestsByPatient(id));
    }

    @GetMapping("/doctor/{id}")
    public ResponseEntity<List<LabTestDTO>> getLabTestsByDoctor(@PathVariable Long id) {
        return ResponseEntity.ok(labTestService.getTestsByDoctor(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<LabTestDTO>> getLabTestsByStatus(@PathVariable LabTest.TestStatus status) {
        return ResponseEntity.ok(labTestService.getTestsByStatus(status));
    }

    @PostMapping
    public ResponseEntity<LabTestDTO> orderLabTest(@Valid @RequestBody LabTestDTO labTestDTO) {
        LabTestDTO createdLabTest = labTestService.orderTest(labTestDTO);
        return new ResponseEntity<>(createdLabTest, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LabTestDTO> updateLabTest(@PathVariable Long id, @Valid @RequestBody LabTestDTO labTestDTO) {
        return ResponseEntity.ok(labTestService.updateLabTest(id, labTestDTO));
    }

    @PutMapping("/{id}/results")
    public ResponseEntity<LabTestDTO> uploadResults(@PathVariable Long id,
            @RequestBody Map<String, String> resultsData) {
        String testResults = resultsData.get("testResults");
        String resultFilePath = resultsData.get("resultFilePath");
        String remarks = resultsData.get("remarks");

        return ResponseEntity.ok(labTestService.uploadResults(id, testResults, resultFilePath, remarks));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLabTest(@PathVariable Long id) {
        labTestService.deleteLabTest(id);
        return ResponseEntity.noContent().build();
    }
}
