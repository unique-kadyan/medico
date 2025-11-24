package com.kaddy.controller;

import com.kaddy.dto.HospitalDTO;
import com.kaddy.dto.HospitalRegistrationRequest;
import com.kaddy.model.enums.SubscriptionPlan;
import com.kaddy.service.FileStorageService;
import com.kaddy.service.HospitalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
@Slf4j
public class HospitalController {

    private final HospitalService hospitalService;
    private final FileStorageService fileStorageService;

    @PostMapping("/register")
    public ResponseEntity<HospitalDTO> registerHospital(@Valid @RequestBody HospitalRegistrationRequest request) {
        HospitalDTO hospital = hospitalService.registerHospital(request);
        return new ResponseEntity<>(hospital, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @hospitalSecurityService.isHospitalMember(#id)")
    public ResponseEntity<HospitalDTO> getHospitalById(@PathVariable Long id) {
        return ResponseEntity.ok(hospitalService.getHospitalById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<HospitalDTO> getHospitalByCode(@PathVariable String code) {
        return ResponseEntity.ok(hospitalService.getHospitalByCode(code));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<HospitalDTO>> getAllHospitals() {
        return ResponseEntity.ok(hospitalService.getAllHospitals());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @hospitalSecurityService.isHospitalAdmin(#id)")
    public ResponseEntity<HospitalDTO> updateHospital(@PathVariable Long id,
            @Valid @RequestBody HospitalDTO hospitalDTO) {
        return ResponseEntity.ok(hospitalService.updateHospital(id, hospitalDTO));
    }

    @PostMapping("/{id}/upgrade")
    @PreAuthorize("@hospitalSecurityService.isHospitalAdmin(#id)")
    public ResponseEntity<HospitalDTO> upgradePlan(@PathVariable Long id, @RequestParam SubscriptionPlan plan) {
        return ResponseEntity.ok(hospitalService.upgradePlan(id, plan));
    }

    @GetMapping("/{id}/subscription/validate")
    public ResponseEntity<Void> validateSubscription(@PathVariable Long id) {
        hospitalService.validateSubscription(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/plans")
    public ResponseEntity<SubscriptionPlan[]> getAvailablePlans() {
        return ResponseEntity.ok(SubscriptionPlan.values());
    }

    @PostMapping(value = "/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or @hospitalSecurityService.isHospitalAdmin(#id)")
    public ResponseEntity<Map<String, String>> uploadLogo(@PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only image files are allowed"));
            }

            if (file.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("error", "File size must be less than 2MB"));
            }

            String logoPath = fileStorageService.storeFile(file, "hospital-logos");

            HospitalDTO hospital = hospitalService.updateHospitalLogo(id, "/uploads/" + logoPath);

            log.info("Logo uploaded for hospital {}: {}", id, logoPath);

            return ResponseEntity.ok(Map.of("logoUrl", hospital.getLogoUrl(), "message", "Logo uploaded successfully"));
        } catch (IOException e) {
            log.error("Failed to upload logo for hospital {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload logo: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/logo")
    @PreAuthorize("hasRole('ADMIN') or @hospitalSecurityService.isHospitalAdmin(#id)")
    public ResponseEntity<Map<String, String>> deleteLogo(@PathVariable Long id) {
        try {
            HospitalDTO hospital = hospitalService.getHospitalById(id);
            if (hospital.getLogoUrl() != null && !hospital.getLogoUrl().isEmpty()) {
                String filePath = hospital.getLogoUrl().replace("/uploads/", "");
                fileStorageService.deleteFile(filePath);
            }
            hospitalService.updateHospitalLogo(id, null);
            log.info("Logo deleted for hospital {}", id);
            return ResponseEntity.ok(Map.of("message", "Logo deleted successfully"));
        } catch (IOException e) {
            log.error("Failed to delete logo for hospital {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete logo: " + e.getMessage()));
        }
    }
}
