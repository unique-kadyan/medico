package com.kaddy.repository;

import com.kaddy.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByDoctorId(String doctorId);

    List<Doctor> findBySpecialization(String specialization);

    List<Doctor> findByDepartment(String department);

    @Query("SELECT d FROM Doctor d WHERE d.availableForConsultation = true")
    List<Doctor> findAllAvailableDoctors();

    @Query("SELECT d FROM Doctor d WHERE d.active = true ORDER BY d.yearsOfExperience DESC")
    List<Doctor> findAllActiveDoctorsOrderedByExperience();

    boolean existsByDoctorId(String doctorId);

    boolean existsByLicenseNumber(String licenseNumber);
}
