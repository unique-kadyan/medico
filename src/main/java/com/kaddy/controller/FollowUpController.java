package com.kaddy.controller;

import com.kaddy.dto.FollowUpDTO;
import com.kaddy.model.FollowUp;
import com.kaddy.service.FollowUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/followups")
@RequiredArgsConstructor
public class FollowUpController {

    private final FollowUpService followUpService;

    @GetMapping
    public ResponseEntity<List<FollowUpDTO>> getAllFollowUps() {
        return ResponseEntity.ok(followUpService.getAllFollowUps());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FollowUpDTO> getFollowUpById(@PathVariable Long id) {
        return ResponseEntity.ok(followUpService.getFollowUpById(id));
    }

    @GetMapping("/patient/{id}")
    public ResponseEntity<List<FollowUpDTO>> getFollowUpsByPatient(@PathVariable Long id) {
        return ResponseEntity.ok(followUpService.getFollowUpsByPatient(id));
    }

    @GetMapping("/doctor/{id}")
    public ResponseEntity<List<FollowUpDTO>> getFollowUpsByDoctor(@PathVariable Long id) {
        return ResponseEntity.ok(followUpService.getFollowUpsByDoctor(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<FollowUpDTO>> getFollowUpsByStatus(@PathVariable FollowUp.FollowUpStatus status) {
        return ResponseEntity.ok(followUpService.getFollowUpsByStatus(status));
    }

    @GetMapping("/patient/{id}/status/{status}")
    public ResponseEntity<List<FollowUpDTO>> getFollowUpsByPatientAndStatus(@PathVariable Long id,
            @PathVariable FollowUp.FollowUpStatus status) {
        return ResponseEntity.ok(followUpService.getFollowUpsByPatientAndStatus(id, status));
    }

    @PostMapping
    public ResponseEntity<FollowUpDTO> scheduleFollowUp(@Valid @RequestBody FollowUpDTO followUpDTO) {
        FollowUpDTO createdFollowUp = followUpService.scheduleFollowUp(followUpDTO);
        return new ResponseEntity<>(createdFollowUp, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FollowUpDTO> updateFollowUp(@PathVariable Long id,
            @Valid @RequestBody FollowUpDTO followUpDTO) {
        return ResponseEntity.ok(followUpService.updateFollowUp(id, followUpDTO));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<FollowUpDTO> completeFollowUp(@PathVariable Long id) {
        return ResponseEntity.ok(followUpService.completeFollowUp(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFollowUp(@PathVariable Long id) {
        followUpService.deleteFollowUp(id);
        return ResponseEntity.noContent().build();
    }
}
