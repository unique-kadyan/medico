package com.kaddy.service;

import com.kaddy.model.Document;
import com.kaddy.model.PendingUser;
import com.kaddy.model.User;
import com.kaddy.model.enums.UserRole;
import com.kaddy.repository.DocumentRepository;
import com.kaddy.repository.PendingUserRepository;
import com.kaddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PendingUserService {

    private final PendingUserRepository pendingUserRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Transactional
    public PendingUser createPendingUser(PendingUser pendingUser, List<MultipartFile> documents) throws IOException {
        if (userRepository.existsByUsername(pendingUser.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(pendingUser.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (pendingUserRepository.existsByUsername(pendingUser.getUsername())) {
            throw new RuntimeException("Registration request with this username already pending");
        }
        if (pendingUserRepository.existsByEmail(pendingUser.getEmail())) {
            throw new RuntimeException("Registration request with this email already pending");
        }

        pendingUser.setPassword(passwordEncoder.encode(pendingUser.getPassword()));

        pendingUser.setStatus("PENDING");
        pendingUser.setRequestedAt(LocalDateTime.now());

        PendingUser savedPendingUser = pendingUserRepository.save(pendingUser);

        if (documents != null && !documents.isEmpty()) {
            for (MultipartFile file : documents) {
                if (!file.isEmpty()) {
                    String filePath = fileStorageService.storeFile(file, "govt-ids/" + savedPendingUser.getId());

                    Document document = new Document();
                    document.setDocumentType("GOVT_ID");
                    document.setFileName(file.getOriginalFilename());
                    document.setFilePath(filePath);
                    document.setFileType(file.getContentType());
                    document.setFileSize(file.getSize());
                    document.setUploadedBy(pendingUser.getUsername());
                    document.setPendingUser(savedPendingUser);
                    document.setUploadedAt(LocalDateTime.now());

                    documentRepository.save(document);
                }
            }
        }

        return savedPendingUser;
    }

    @Transactional(readOnly = true)
    public List<PendingUser> getAllPendingUsers() {
        return pendingUserRepository.findByStatus("PENDING");
    }

    @Transactional(readOnly = true)
    public List<PendingUser> getPendingUsersByRole(UserRole role) {
        return pendingUserRepository.findByStatusAndRequestedRole("PENDING", role);
    }

    @Transactional(readOnly = true)
    public PendingUser getPendingUserById(Long id) {
        return pendingUserRepository.findById(id).orElseThrow(() -> new RuntimeException("Pending user not found"));
    }

    @Transactional
    public User approveRegistration(Long pendingUserId, Long approvedBy) {
        PendingUser pendingUser = getPendingUserById(pendingUserId);

        if (!"PENDING".equals(pendingUser.getStatus())) {
            throw new RuntimeException("Registration request is not pending");
        }

        User user = new User();
        user.setUsername(pendingUser.getUsername());
        user.setEmail(pendingUser.getEmail());
        user.setPassword(pendingUser.getPassword());
        if (pendingUser.getFirstName() != null) {
            user.setFirstName(pendingUser.getFirstName());
        }
        if (pendingUser.getLastName() != null) {
            user.setLastName(pendingUser.getLastName());
        }
        if (pendingUser.getPhone() != null) {
            user.setPhone(pendingUser.getPhone());
        }
        user.setRole(pendingUser.getRequestedRole());
        user.setActive(true);

        User savedUser = userRepository.save(user);

        List<Document> userDocuments = documentRepository.findByPendingUserId(pendingUserId);
        for (Document doc : userDocuments) {
            doc.setUser(savedUser);
            doc.setPendingUser(null);
            documentRepository.save(doc);
        }

        pendingUser.setStatus("APPROVED");
        pendingUser.setApprovedBy(approvedBy);
        pendingUser.setReviewedAt(LocalDateTime.now());
        pendingUserRepository.save(pendingUser);

        return savedUser;
    }

    @Transactional
    public void rejectRegistration(Long pendingUserId, Long rejectedBy, String reason) {
        PendingUser pendingUser = getPendingUserById(pendingUserId);

        if (!"PENDING".equals(pendingUser.getStatus())) {
            throw new RuntimeException("Registration request is not pending");
        }

        pendingUser.setStatus("REJECTED");
        pendingUser.setApprovedBy(rejectedBy);
        pendingUser.setRejectionReason(reason);
        pendingUser.setReviewedAt(LocalDateTime.now());
        pendingUserRepository.save(pendingUser);
    }

    @Transactional(readOnly = true)
    public boolean canApprove(UserRole approverRole, UserRole requestedRole) {
        if (approverRole == UserRole.ADMIN) {
            return true;
        }

        if (approverRole == UserRole.DOCTOR_SUPERVISOR && requestedRole == UserRole.DOCTOR) {
            return true;
        }

        if ((approverRole == UserRole.NURSE_MANAGER || approverRole == UserRole.NURSE_SUPERVISOR)
                && requestedRole == UserRole.NURSE) {
            return true;
        }

        return false;
    }

    @Transactional(readOnly = true)
    public List<Document> getDocumentsForPendingUser(Long pendingUserId) {
        return documentRepository.findByPendingUserId(pendingUserId);
    }
}
