package com.kaddy.repository;

import com.kaddy.model.MedicationRequest;
import com.kaddy.model.enums.MedicationRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationRequestRepository extends JpaRepository<MedicationRequest, Long> {

    List<MedicationRequest> findByStatus(MedicationRequestStatus status);

    List<MedicationRequest> findByRequestedById(Long requestedById);

    List<MedicationRequest> findByStatusOrderByRequestDateDesc(MedicationRequestStatus status);
}
