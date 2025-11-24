package com.kaddy.repository;

import com.kaddy.model.Appointment;
import com.kaddy.model.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByStatus(AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId "
            + "AND a.appointmentDateTime BETWEEN :startDate AND :endDate " + "ORDER BY a.appointmentDateTime")
    List<Appointment> findDoctorAppointmentsBetweenDates(@Param("doctorId") Long doctorId,
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId "
            + "AND a.appointmentDateTime BETWEEN :startDate AND :endDate " + "ORDER BY a.appointmentDateTime")
    List<Appointment> findPatientAppointmentsBetweenDates(@Param("patientId") Long patientId,
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDateTime BETWEEN :startDate AND :endDate "
            + "ORDER BY a.appointmentDateTime")
    List<Appointment> findAppointmentsBetweenDates(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doctorId "
            + "AND a.appointmentDateTime BETWEEN :startDateTime AND :endDateTime "
            + "AND a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    long countDoctorAppointmentsInTimeSlot(@Param("doctorId") Long doctorId,
            @Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);
}
