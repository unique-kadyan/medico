package com.kaddy.repository;

import com.kaddy.model.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

    List<InvoiceItem> findByInvoiceId(Long invoiceId);

    @Query("SELECT ii FROM InvoiceItem ii WHERE ii.invoice.hospital.id = :hospitalId "
            + "AND ii.serviceDate BETWEEN :startDate AND :endDate")
    List<InvoiceItem> findByHospitalIdAndDateRange(@Param("hospitalId") Long hospitalId,
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT ii.category, SUM(ii.lineTotal) FROM InvoiceItem ii " + "WHERE ii.invoice.hospital.id = :hospitalId "
            + "AND ii.serviceDate BETWEEN :startDate AND :endDate " + "GROUP BY ii.category")
    List<Object[]> getRevenueByCategory(@Param("hospitalId") Long hospitalId, @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT ii.performedBy.id, CONCAT(ii.performedBy.user.firstName, ' ', ii.performedBy.user.lastName), SUM(ii.lineTotal) "
            + "FROM InvoiceItem ii WHERE ii.invoice.hospital.id = :hospitalId " + "AND ii.performedBy IS NOT NULL "
            + "AND ii.serviceDate BETWEEN :startDate AND :endDate "
            + "GROUP BY ii.performedBy.id, ii.performedBy.user.firstName, ii.performedBy.user.lastName")
    List<Object[]> getRevenueByDoctor(@Param("hospitalId") Long hospitalId, @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT ii.itemName, COUNT(ii), SUM(ii.lineTotal) FROM InvoiceItem ii "
            + "WHERE ii.invoice.hospital.id = :hospitalId " + "AND ii.serviceDate BETWEEN :startDate AND :endDate "
            + "GROUP BY ii.itemName ORDER BY COUNT(ii) DESC")
    List<Object[]> getMostUsedServices(@Param("hospitalId") Long hospitalId, @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
