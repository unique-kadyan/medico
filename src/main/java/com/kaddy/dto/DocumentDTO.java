package com.kaddy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {
    private Long id;
    private String documentType;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
    private boolean verified;
    private Long verifiedBy;
    private LocalDateTime verifiedAt;
}
