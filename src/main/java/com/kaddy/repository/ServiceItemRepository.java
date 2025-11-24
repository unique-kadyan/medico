package com.kaddy.repository;

import com.kaddy.model.ServiceItem;
import com.kaddy.model.enums.ServiceCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {

    List<ServiceItem> findByHospitalIdAndIsActiveTrue(Long hospitalId);

    Page<ServiceItem> findByHospitalIdAndIsActiveTrue(Long hospitalId, Pageable pageable);

    List<ServiceItem> findByHospitalIdAndCategoryAndIsActiveTrue(Long hospitalId, ServiceCategory category);

    Optional<ServiceItem> findByHospitalIdAndCode(Long hospitalId, String code);

    @Query("SELECT s FROM ServiceItem s WHERE s.hospital.id = :hospitalId AND s.isActive = true "
            + "AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ServiceItem> searchByNameOrCode(@Param("hospitalId") Long hospitalId, @Param("search") String search,
            Pageable pageable);

    boolean existsByHospitalIdAndCode(Long hospitalId, String code);
}
