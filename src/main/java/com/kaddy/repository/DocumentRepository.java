package com.kaddy.repository;

import com.kaddy.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByPendingUserId(Long pendingUserId);

    List<Document> findByUserId(Long userId);

    List<Document> findByUploadedBy(String uploadedBy);

    List<Document> findByDocumentType(String documentType);

    List<Document> findByVerified(boolean verified);
}
