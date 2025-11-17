package com.kaddy.repository;

import com.kaddy.model.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {

    Optional<Medication> findByMedicationCode(String medicationCode);

    List<Medication> findByCategory(String category);

    List<Medication> findByNameContainingIgnoreCase(String name);

    @Query("SELECT m FROM Medication m WHERE m.stockQuantity <= m.reorderLevel")
    List<Medication> findLowStockMedications();

    @Query("SELECT m FROM Medication m WHERE m.expiryDate < :date")
    List<Medication> findExpiredMedications(LocalDate date);

    @Query("SELECT m FROM Medication m WHERE m.expiryDate BETWEEN :startDate AND :endDate")
    List<Medication> findMedicationsExpiringBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT m FROM Medication m WHERE m.active = true ORDER BY m.name")
    List<Medication> findAllActiveMedications();

    boolean existsByMedicationCode(String medicationCode);
}
