package com.kaddy.repository;

import com.kaddy.model.Ward;
import com.kaddy.model.enums.WardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WardRepository extends JpaRepository<Ward, Long> {

    List<Ward> findByHospitalIdAndIsActiveTrue(Long hospitalId);

    List<Ward> findByHospitalIdAndWardTypeAndIsActiveTrue(Long hospitalId, WardType wardType);

    Optional<Ward> findByHospitalIdAndCode(Long hospitalId, String code);

    @Query("SELECT w FROM Ward w WHERE w.hospital.id = :hospitalId AND w.availableBeds > 0 AND w.isActive = true")
    List<Ward> findWardsWithAvailableBeds(@Param("hospitalId") Long hospitalId);

    @Query("SELECT SUM(w.totalBeds) FROM Ward w WHERE w.hospital.id = :hospitalId AND w.isActive = true")
    Integer getTotalBedsByHospital(@Param("hospitalId") Long hospitalId);

    @Query("SELECT SUM(w.availableBeds) FROM Ward w WHERE w.hospital.id = :hospitalId AND w.isActive = true")
    Integer getAvailableBedsByHospital(@Param("hospitalId") Long hospitalId);

    boolean existsByHospitalIdAndCode(Long hospitalId, String code);
}
