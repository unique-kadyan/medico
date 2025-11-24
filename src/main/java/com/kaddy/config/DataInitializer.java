package com.kaddy.config;

import com.kaddy.model.Doctor;
import com.kaddy.model.Patient;
import com.kaddy.model.User;
import com.kaddy.model.enums.BloodGroup;
import com.kaddy.model.enums.Gender;
import com.kaddy.model.enums.UserRole;
import com.kaddy.repository.DoctorRepository;
import com.kaddy.repository.PatientRepository;
import com.kaddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Profile({"dev", "default"})
    public CommandLineRunner initializeTestUsers() {
        return args -> {
            log.info("Initializing test users for development environment...");

            createUserIfNotExists("admin", "admin@medico.com", "admin123", "Admin", "User", "+91 81684 81271",
                    UserRole.ADMIN);

            createUserIfNotExists("doctor", "doctor@medico.com", "doctor123", "John", "Doe", "+91 98765 43210",
                    UserRole.DOCTOR);

            createUserIfNotExists("nurse", "nurse@medico.com", "nurse123", "Jane", "Smith", "+91 98765 43211",
                    UserRole.NURSE);

            createUserIfNotExists("pharmacist", "pharmacist@medico.com", "pharmacist123", "Emily", "Johnson",
                    "+91 98765 43212", UserRole.PHARMACIST);

            createUserIfNotExists("labtech", "labtech@medico.com", "labtech123", "Michael", "Brown", "+91 98765 43213",
                    UserRole.LAB_TECHNICIAN);

            createUserIfNotExists("receptionist", "receptionist@medico.com", "receptionist123", "Sarah", "Wilson",
                    "+91 98765 43214", UserRole.RECEPTIONIST);

            log.info("Test users initialization completed!");

            log.info("Initializing test doctors...");
            createDoctorIfNotExists("DOC001", "John", "Doe", "Cardiology", "LIC-12345", "+91 98765 43210",
                    "doctor@medico.com", "Cardiology", 10, "MBBS, MD (Cardiology)",
                    "Experienced cardiologist with 10 years of practice");

            createDoctorIfNotExists("DOC002", "Emily", "Smith", "Pediatrics", "LIC-67890", "+91 98765 43215",
                    "emily.smith@medico.com", "Pediatrics", 8, "MBBS, DCH",
                    "Pediatric specialist with focus on child healthcare");

            createDoctorIfNotExists("DOC003", "Michael", "Chen", "Orthopedics", "LIC-11223", "+91 98765 43216",
                    "michael.chen@medico.com", "Orthopedics", 12, "MBBS, MS (Orthopedics)",
                    "Expert in joint replacement and sports injuries");

            log.info("Test doctors initialization completed!");

            log.info("Initializing test patients...");
            createPatientIfNotExists("PAT001", "Alice", "Johnson", LocalDate.of(1990, 5, 15), Gender.FEMALE,
                    "+91 98765 43220", "alice.johnson@email.com", "123 Main St, Mumbai", "Bob Johnson",
                    "+91 98765 43221", BloodGroup.A_POSITIVE, "Penicillin", "Hypertension");

            createPatientIfNotExists("PAT002", "Robert", "Williams", LocalDate.of(1985, 8, 20), Gender.MALE,
                    "+91 98765 43222", "robert.williams@email.com", "456 Park Ave, Delhi", "Mary Williams",
                    "+91 98765 43223", BloodGroup.O_POSITIVE, "None", "Diabetes Type 2");

            createPatientIfNotExists("PAT003", "Sophia", "Martinez", LocalDate.of(2005, 3, 10), Gender.FEMALE,
                    "+91 98765 43224", "sophia.martinez@email.com", "789 Lake Road, Bangalore", "Carlos Martinez",
                    "+91 98765 43225", BloodGroup.B_POSITIVE, "Aspirin", "Asthma");

            createPatientIfNotExists("PAT004", "James", "Davis", LocalDate.of(1978, 12, 5), Gender.MALE,
                    "+91 98765 43226", "james.davis@email.com", "321 Hill Street, Chennai", "Linda Davis",
                    "+91 98765 43227", BloodGroup.AB_NEGATIVE, "None", "None");

            log.info("Test patients initialization completed!");
        };
    }

    private void createUserIfNotExists(String username, String email, String password, String firstName,
            String lastName, String phone, UserRole role) {
        var existingUserOptional = userRepository.findByUsername(username).or(() -> userRepository.findByEmail(email));

        if (existingUserOptional.isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhone(phone);
            user.setRole(role);
            user.setEnabled(true);

            userRepository.save(user);
            log.info("Created test user: {} ({}) with role: {}", username, email, role);
        } else {
            User existingUser = existingUserOptional.get();
            String encodedPassword = passwordEncoder.encode(password);

            existingUser.setPassword(encodedPassword);
            existingUser.setEnabled(true);
            userRepository.save(existingUser);
            log.info("Updated password for existing test user: {} ({}) with role: {}", username, email, role);
        }
    }

    private void createDoctorIfNotExists(String doctorId, String firstName, String lastName, String specialization,
            String licenseNumber, String phone, String email, String department, Integer yearsOfExperience,
            String qualification, String about) {
        var existingDoctor = doctorRepository.findByDoctorId(doctorId);
        if (existingDoctor.isEmpty()) {
            var userOptional = userRepository.findByEmail(email);

            Doctor doctor = new Doctor();
            doctor.setDoctorId(doctorId);
            doctor.setFirstName(firstName);
            doctor.setLastName(lastName);
            doctor.setSpecialization(specialization);
            doctor.setLicenseNumber(licenseNumber);
            doctor.setPhone(phone);
            doctor.setEmail(email);
            doctor.setDepartment(department);
            doctor.setYearsOfExperience(yearsOfExperience);
            doctor.setQualification(qualification);
            doctor.setAbout(about);
            doctor.setAvailableForConsultation(true);

            userOptional.ifPresent(doctor::setUser);

            doctorRepository.save(doctor);
            log.info("Created test doctor: Dr. {} {} ({})", firstName, lastName, doctorId);
        } else {
            log.info("Doctor already exists: {}", doctorId);
        }
    }

    private void createPatientIfNotExists(String patientId, String firstName, String lastName, LocalDate dateOfBirth,
            Gender gender, String phone, String email, String address, String emergencyContact,
            String emergencyContactPhone, BloodGroup bloodGroup, String allergies, String chronicConditions) {
        var existingPatient = patientRepository.findByPatientId(patientId);
        if (existingPatient.isEmpty()) {
            Patient patient = new Patient();
            patient.setPatientId(patientId);
            patient.setFirstName(firstName);
            patient.setLastName(lastName);
            patient.setDateOfBirth(dateOfBirth);
            patient.setGender(gender);
            patient.setPhone(phone);
            patient.setEmail(email);
            patient.setAddress(address);
            patient.setEmergencyContact(emergencyContact);
            patient.setEmergencyContactPhone(emergencyContactPhone);
            patient.setBloodGroup(bloodGroup);
            patient.setAllergies(allergies);
            patient.setChronicConditions(chronicConditions);

            patientRepository.save(patient);
            log.info("Created test patient: {} {} ({})", firstName, lastName, patientId);
        } else {
            log.info("Patient already exists: {}", patientId);
        }
    }
}
