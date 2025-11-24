package com.kaddy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lab_tests")
public class LabTest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_technician_id")
    private User labTechnician;

    @Column(name = "test_name", nullable = false, length = 200)
    private String testName;

    @Column(name = "test_type", length = 100)
    private String testType;

    @Column(name = "ordered_date", nullable = false)
    private LocalDateTime orderedDate;

    @Column(name = "sample_collected_date")
    private LocalDateTime sampleCollectedDate;

    @Column(name = "result_date")
    private LocalDateTime resultDate;

    @Column(name = "test_results", columnDefinition = "TEXT")
    private String testResults;

    @Column(name = "result_file_path", length = 500)
    private String resultFilePath;

    @Column(name = "normal_range", length = 200)
    private String normalRange;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TestStatus status = TestStatus.ORDERED;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private TestPriority priority = TestPriority.NORMAL;

    @Column(name = "urgent")
    private Boolean urgent = false;

    public enum TestStatus {
        ORDERED, SAMPLE_COLLECTED, IN_PROGRESS, COMPLETED, CANCELLED, REJECTED
    }

    public enum TestPriority {
        ROUTINE, NORMAL, URGENT, STAT
    }
}
