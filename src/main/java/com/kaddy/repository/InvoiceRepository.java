package com.kaddy.repository;

import com.kaddy.model.Invoice;
import com.kaddy.model.enums.BillingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Page<Invoice> findByHospitalId(Long hospitalId, Pageable pageable);

    Page<Invoice> findByHospitalIdAndStatus(Long hospitalId, BillingStatus status, Pageable pageable);

    List<Invoice> findByPatientId(Long patientId);

    Page<Invoice> findByPatientId(Long patientId, Pageable pageable);

    List<Invoice> findByAdmissionId(Long admissionId);

    List<Invoice> findByAppointmentId(Long appointmentId);

    @Query("SELECT i FROM Invoice i WHERE i.hospital.id = :hospitalId AND i.invoiceDate BETWEEN :startDate AND :endDate")
    List<Invoice> findByHospitalIdAndDateRange(@Param("hospitalId") Long hospitalId,
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT i FROM Invoice i WHERE i.hospital.id = :hospitalId AND i.status IN :statuses")
    Page<Invoice> findByHospitalIdAndStatuses(@Param("hospitalId") Long hospitalId,
            @Param("statuses") List<BillingStatus> statuses, Pageable pageable);

    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.hospital.id = :hospitalId "
            + "AND i.invoiceDate BETWEEN :startDate AND :endDate AND i.status != 'CANCELLED'")
    BigDecimal getTotalRevenue(@Param("hospitalId") Long hospitalId, @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(i.paidAmount) FROM Invoice i WHERE i.hospital.id = :hospitalId "
            + "AND i.invoiceDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalCollected(@Param("hospitalId") Long hospitalId, @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(i.balanceAmount) FROM Invoice i WHERE i.hospital.id = :hospitalId "
            + "AND i.status IN ('PENDING', 'PARTIAL_PAID', 'OVERDUE')")
    BigDecimal getTotalOutstanding(@Param("hospitalId") Long hospitalId);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.hospital.id = :hospitalId AND i.status = :status")
    Long countByHospitalIdAndStatus(@Param("hospitalId") Long hospitalId, @Param("status") BillingStatus status);

    @Query("SELECT i FROM Invoice i WHERE i.hospital.id = :hospitalId AND "
            + "(i.invoiceNumber LIKE %:search% OR i.patient.firstName LIKE %:search% OR i.patient.lastName LIKE %:search%)")
    Page<Invoice> searchInvoices(@Param("hospitalId") Long hospitalId, @Param("search") String search,
            Pageable pageable);

    @Query("SELECT MAX(i.invoiceNumber) FROM Invoice i WHERE i.hospital.id = :hospitalId AND i.invoiceNumber LIKE :prefix%")
    String findLastInvoiceNumber(@Param("hospitalId") Long hospitalId, @Param("prefix") String prefix);
}
