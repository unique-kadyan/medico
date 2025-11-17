package com.kaddy.repository;

import com.kaddy.model.FollowUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowUpRepository extends JpaRepository<FollowUp, Long> {

    List<FollowUp> findByPatientId(Long patientId);

    List<FollowUp> findByDoctorId(Long doctorId);

    List<FollowUp> findByStatus(FollowUp.FollowUpStatus status);

    List<FollowUp> findByPatientIdAndStatusOrderByFollowupDateDesc(Long patientId, FollowUp.FollowUpStatus status);
}
