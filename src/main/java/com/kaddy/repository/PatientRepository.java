package com.kaddy.repository;

import com.kaddy.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByPatientId(String patientId);

    @Query("SELECT p FROM Patient p WHERE p.user.id = :userId")
    Optional<Patient> findByUserId(@Param("userId") Long userId);

    List<Patient> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String firstName, String lastName);

    @Query("SELECT p FROM Patient p WHERE p.phone = :phone")
    Optional<Patient> findByPhone(String phone);

    @Query("SELECT p FROM Patient p WHERE p.email = :email")
    Optional<Patient> findByEmail(String email);

    boolean existsByPatientId(String patientId);

    @Query("SELECT p FROM Patient p WHERE p.active = true ORDER BY p.createdAt DESC")
    List<Patient> findAllActivePatients();
}
