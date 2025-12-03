package com.kaddy.repository;

import com.kaddy.model.PrescriptionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, Long> {

    List<PrescriptionItem> findByPrescriptionId(Long prescriptionId);

    @Query("SELECT pi FROM PrescriptionItem pi WHERE pi.prescription.id = :prescriptionId AND pi.active = true")
    List<PrescriptionItem> findActiveItemsByPrescriptionId(@Param("prescriptionId") Long prescriptionId);

    @Query("SELECT pi FROM PrescriptionItem pi WHERE pi.medication.id = :medicationId AND pi.active = true")
    List<PrescriptionItem> findByMedicationId(@Param("medicationId") Long medicationId);
}
