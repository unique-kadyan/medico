package com.kaddy.repository;

import com.kaddy.model.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    List<Vendor> findByHospitalIdAndIsActiveTrue(Long hospitalId);

    Page<Vendor> findByHospitalIdAndIsActiveTrue(Long hospitalId, Pageable pageable);

    Optional<Vendor> findByHospitalIdAndVendorCode(Long hospitalId, String vendorCode);

    @Query("SELECT v FROM Vendor v WHERE v.hospital.id = :hospitalId AND v.isActive = true "
            + "AND (LOWER(v.name) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(v.vendorCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Vendor> searchVendors(@Param("hospitalId") Long hospitalId, @Param("search") String search, Pageable pageable);

    boolean existsByHospitalIdAndVendorCode(Long hospitalId, String vendorCode);
}
