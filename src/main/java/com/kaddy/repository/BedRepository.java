package com.kaddy.repository;

import com.kaddy.model.Bed;
import com.kaddy.model.enums.BedStatus;
import com.kaddy.model.enums.BedType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BedRepository extends JpaRepository<Bed, Long> {

    List<Bed> findByHospitalIdAndActiveTrue(Long hospitalId);

    List<Bed> findByWardIdAndActiveTrue(Long wardId);

    List<Bed> findByHospitalIdAndStatusAndActiveTrue(Long hospitalId, BedStatus status);

    List<Bed> findByWardIdAndStatusAndActiveTrue(Long wardId, BedStatus status);

    List<Bed> findByHospitalIdAndBedTypeAndActiveTrue(Long hospitalId, BedType bedType);

    Optional<Bed> findByHospitalIdAndBedNumber(Long hospitalId, String bedNumber);

    @Query("SELECT COUNT(b) FROM Bed b WHERE b.hospital.id = :hospitalId AND b.status = :status AND b.active = true")
    long countByHospitalIdAndStatus(@Param("hospitalId") Long hospitalId, @Param("status") BedStatus status);

    @Query("SELECT COUNT(b) FROM Bed b WHERE b.ward.id = :wardId AND b.status = :status AND b.active = true")
    long countByWardIdAndStatus(@Param("wardId") Long wardId, @Param("status") BedStatus status);

    @Query("SELECT b FROM Bed b WHERE b.hospital.id = :hospitalId AND b.status = 'AVAILABLE' AND b.bedType = :bedType AND b.active = true ORDER BY b.ward.name, b.bedNumber")
    List<Bed> findAvailableBedsByType(@Param("hospitalId") Long hospitalId, @Param("bedType") BedType bedType);

    @Query("SELECT b FROM Bed b WHERE b.ward.id = :wardId AND b.status = 'AVAILABLE' AND b.active = true ORDER BY b.bedNumber")
    List<Bed> findAvailableBedsByWard(@Param("wardId") Long wardId);

    boolean existsByHospitalIdAndBedNumber(Long hospitalId, String bedNumber);
}
