package com.kaddy.dto;

import com.kaddy.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingUserDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private UserRole requestedRole;
    private String status;
    private Long requestedBy;
    private Long approvedBy;
    private String rejectionReason;
    private LocalDateTime requestedAt;
    private LocalDateTime reviewedAt;
    private List<DocumentDTO> documents;
    private int documentCount;
}
