package com.kaddy.model;

import com.kaddy.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pending_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String firstName;
    private String lastName;
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole requestedRole;

    @OneToMany(mappedBy = "pendingUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();

    @Column(nullable = false)
    private String status = "PENDING";

    private Long requestedBy;

    private Long approvedBy;

    private String rejectionReason;

    @Column(nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    private LocalDateTime reviewedAt;

    @PrePersist
    protected void onCreate() {
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "PENDING";
        }
    }

    public void addDocument(Document document) {
        documents.add(document);
        document.setPendingUser(this);
    }

    public void removeDocument(Document document) {
        documents.remove(document);
        document.setPendingUser(null);
    }
}
