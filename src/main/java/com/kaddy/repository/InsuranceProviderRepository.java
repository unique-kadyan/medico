package com.kaddy.repository;

import com.kaddy.model.InsuranceProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsuranceProviderRepository extends JpaRepository<InsuranceProvider, Long> {

    List<InsuranceProvider> findByIsActiveTrue();

    Page<InsuranceProvider> findByIsActiveTrue(Pageable pageable);

    Optional<InsuranceProvider> findByProviderCode(String providerCode);

    @Query("SELECT ip FROM InsuranceProvider ip WHERE ip.isActive = true "
            + "AND (LOWER(ip.name) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(ip.providerCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<InsuranceProvider> searchProviders(@Param("search") String search, Pageable pageable);

    boolean existsByProviderCode(String providerCode);
}
