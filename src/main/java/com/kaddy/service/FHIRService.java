package com.kaddy.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.kaddy.dto.fhir.FHIRPatientDTO;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.Doctor;
import com.kaddy.repository.DoctorRepository;
import com.kaddy.repository.MedicationRepository;
import com.kaddy.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class FHIRService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final MedicationRepository medicationRepository;
    private final FhirContext fhirContext = FhirContext.forR4();

    @Transactional(readOnly = true)
    public org.hl7.fhir.r4.model.Patient convertToFHIRPatient(Long patientId) {
        com.kaddy.model.Patient internalPatient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        org.hl7.fhir.r4.model.Patient fhirPatient = new org.hl7.fhir.r4.model.Patient();

        fhirPatient.addIdentifier().setSystem("http://medico.healthcare/patient-id")
                .setValue(String.valueOf(internalPatient.getId()));

        HumanName name = fhirPatient.addName();
        name.setFamily(internalPatient.getLastName());
        name.addGiven(internalPatient.getFirstName());

        if (internalPatient.getGender() != null) {
            switch (internalPatient.getGender().toString().toUpperCase()) {
                case "MALE" :
                    fhirPatient.setGender(Enumerations.AdministrativeGender.MALE);
                    break;
                case "FEMALE" :
                    fhirPatient.setGender(Enumerations.AdministrativeGender.FEMALE);
                    break;
                default :
                    fhirPatient.setGender(Enumerations.AdministrativeGender.OTHER);
            }
        }

        if (internalPatient.getDateOfBirth() != null) {
            fhirPatient.setBirthDate(java.sql.Date.valueOf(internalPatient.getDateOfBirth()));
        }

        if (internalPatient.getPhone() != null) {
            fhirPatient.addTelecom().setSystem(ContactPoint.ContactPointSystem.PHONE)
                    .setValue(internalPatient.getPhone());
        }

        if (internalPatient.getEmail() != null) {
            fhirPatient.addTelecom().setSystem(ContactPoint.ContactPointSystem.EMAIL)
                    .setValue(internalPatient.getEmail());
        }

        if (internalPatient.getAddress() != null) {
            Address address = fhirPatient.addAddress();
            address.setText(internalPatient.getAddress());
        }

        fhirPatient.setActive(internalPatient.getActive());

        return fhirPatient;
    }

    @Transactional(readOnly = true)
    public String getFHIRPatientJson(Long patientId) {
        org.hl7.fhir.r4.model.Patient fhirPatient = convertToFHIRPatient(patientId);
        IParser jsonParser = fhirContext.newJsonParser().setPrettyPrint(true);
        return jsonParser.encodeResourceToString(fhirPatient);
    }

    @Transactional(readOnly = true)
    public String getFHIRPatientXml(Long patientId) {
        org.hl7.fhir.r4.model.Patient fhirPatient = convertToFHIRPatient(patientId);
        IParser xmlParser = fhirContext.newXmlParser().setPrettyPrint(true);
        return xmlParser.encodeResourceToString(fhirPatient);
    }

    @Transactional(readOnly = true)
    public Practitioner convertToFHIRPractitioner(Long doctorId) {
        Doctor internalDoctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        Practitioner practitioner = new Practitioner();

        practitioner.addIdentifier().setSystem("http://medico.healthcare/practitioner-id")
                .setValue(String.valueOf(internalDoctor.getId()));

        if (internalDoctor.getLicenseNumber() != null) {
            practitioner.addIdentifier().setSystem("http://medico.healthcare/license-number")
                    .setValue(internalDoctor.getLicenseNumber());
        }

        HumanName name = practitioner.addName();
        name.setFamily(internalDoctor.getUser().getLastName());
        name.addGiven(internalDoctor.getUser().getFirstName());
        name.addPrefix("Dr.");

        if (internalDoctor.getSpecialization() != null) {
            Practitioner.PractitionerQualificationComponent qualification = practitioner.addQualification();
            qualification.getCode().addCoding().setSystem("http://medico.healthcare/specialization")
                    .setCode(internalDoctor.getSpecialization()).setDisplay(internalDoctor.getSpecialization());
        }

        if (internalDoctor.getUser().getPhone() != null) {
            practitioner.addTelecom().setSystem(ContactPoint.ContactPointSystem.PHONE)
                    .setValue(internalDoctor.getUser().getPhone());
        }

        if (internalDoctor.getUser().getEmail() != null) {
            practitioner.addTelecom().setSystem(ContactPoint.ContactPointSystem.EMAIL)
                    .setValue(internalDoctor.getUser().getEmail());
        }

        practitioner.setActive(internalDoctor.getActive());

        return practitioner;
    }

    @Transactional(readOnly = true)
    public String getFHIRPractitionerJson(Long doctorId) {
        Practitioner practitioner = convertToFHIRPractitioner(doctorId);
        IParser jsonParser = fhirContext.newJsonParser().setPrettyPrint(true);
        return jsonParser.encodeResourceToString(practitioner);
    }

    @Transactional(readOnly = true)
    public Observation createVitalSignsObservation(Long patientId, String type, String value, String unit) {
        com.kaddy.model.Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        Observation observation = new Observation();

        observation.setStatus(Observation.ObservationStatus.FINAL);

        observation.addCategory().addCoding().setSystem("http://terminology.hl7.org/CodeSystem/observation-category")
                .setCode("vital-signs").setDisplay("Vital Signs");

        CodeableConcept code = new CodeableConcept();
        switch (type.toLowerCase()) {
            case "blood_pressure" :
                code.addCoding().setSystem("http://loinc.org").setCode("85354-9").setDisplay("Blood pressure panel");
                break;
            case "heart_rate" :
                code.addCoding().setSystem("http://loinc.org").setCode("8867-4").setDisplay("Heart rate");
                break;
            case "temperature" :
                code.addCoding().setSystem("http://loinc.org").setCode("8310-5").setDisplay("Body temperature");
                break;
            case "respiratory_rate" :
                code.addCoding().setSystem("http://loinc.org").setCode("9279-1").setDisplay("Respiratory rate");
                break;
            case "oxygen_saturation" :
                code.addCoding().setSystem("http://loinc.org").setCode("2708-6").setDisplay("Oxygen saturation");
                break;
            default :
                code.addCoding().setSystem("http://medico.healthcare/observation-type").setCode(type).setDisplay(type);
        }
        observation.setCode(code);

        observation.setSubject(new Reference().setReference("Patient/" + patientId)
                .setDisplay(patient.getFirstName() + " " + patient.getLastName()));

        observation.setEffective(new DateTimeType(new Date()));

        try {
            double numValue = Double.parseDouble(value);
            observation
                    .setValue(new Quantity().setValue(numValue).setUnit(unit).setSystem("http://unitsofmeasure.org"));
        } catch (NumberFormatException e) {
            observation.setValue(new StringType(value));
        }

        return observation;
    }

    @Transactional(readOnly = true)
    public String getVitalSignsObservationJson(Long patientId, String type, String value, String unit) {
        Observation observation = createVitalSignsObservation(patientId, type, value, unit);
        IParser jsonParser = fhirContext.newJsonParser().setPrettyPrint(true);
        return jsonParser.encodeResourceToString(observation);
    }

    @Transactional(readOnly = true)
    public org.hl7.fhir.r4.model.MedicationRequest createFHIRMedicationRequest(Long patientId, Long medicationId,
            Long doctorId, String dosage, String frequency) {
        com.kaddy.model.Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        com.kaddy.model.Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Medication not found"));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        org.hl7.fhir.r4.model.MedicationRequest request = new org.hl7.fhir.r4.model.MedicationRequest();

        request.setStatus(org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestStatus.ACTIVE);
        request.setIntent(org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestIntent.ORDER);

        CodeableConcept medicationCodeable = new CodeableConcept();
        medicationCodeable.addCoding().setSystem("http://medico.healthcare/medication")
                .setCode(medication.getMedicationCode())
                .setDisplay(medication.getGenericName() != null ? medication.getGenericName() : medication.getName());
        if (medication.getName() != null) {
            medicationCodeable.setText(medication.getName());
        }
        request.setMedication(medicationCodeable);

        request.setSubject(new Reference().setReference("Patient/" + patientId)
                .setDisplay(patient.getFirstName() + " " + patient.getLastName()));

        request.setRequester(new Reference().setReference("Practitioner/" + doctorId)
                .setDisplay("Dr. " + doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName()));

        Dosage dosageInstruction = request.addDosageInstruction();
        dosageInstruction.setText(dosage + " " + frequency);

        request.setAuthoredOn(new Date());

        return request;
    }

    @Transactional(readOnly = true)
    public String getMedicationRequestJson(Long patientId, Long medicationId, Long doctorId, String dosage,
            String frequency) {
        org.hl7.fhir.r4.model.MedicationRequest request = createFHIRMedicationRequest(patientId, medicationId, doctorId,
                dosage, frequency);
        IParser jsonParser = fhirContext.newJsonParser().setPrettyPrint(true);
        return jsonParser.encodeResourceToString(request);
    }

    @Transactional(readOnly = true)
    public Bundle createPatientBundle(Long patientId) {
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);
        bundle.setTimestamp(new Date());

        org.hl7.fhir.r4.model.Patient fhirPatient = convertToFHIRPatient(patientId);
        bundle.addEntry().setResource(fhirPatient).getRequest().setMethod(Bundle.HTTPVerb.GET)
                .setUrl("Patient/" + patientId);

        return bundle;
    }

    @Transactional(readOnly = true)
    public String getPatientBundleJson(Long patientId) {
        Bundle bundle = createPatientBundle(patientId);
        IParser jsonParser = fhirContext.newJsonParser().setPrettyPrint(true);
        return jsonParser.encodeResourceToString(bundle);
    }

    public FHIRPatientDTO parseFHIRPatient(String fhirJson) {
        IParser parser = fhirContext.newJsonParser();
        org.hl7.fhir.r4.model.Patient fhirPatient = parser.parseResource(org.hl7.fhir.r4.model.Patient.class, fhirJson);

        FHIRPatientDTO dto = new FHIRPatientDTO();

        if (!fhirPatient.getName().isEmpty()) {
            HumanName name = fhirPatient.getNameFirstRep();
            dto.setFirstName(name.getGivenAsSingleString());
            dto.setLastName(name.getFamily());
        }

        if (fhirPatient.getGender() != null) {
            dto.setGender(fhirPatient.getGender().toCode());
        }

        if (fhirPatient.getBirthDate() != null) {
            dto.setDateOfBirth(
                    fhirPatient.getBirthDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        }

        for (ContactPoint telecom : fhirPatient.getTelecom()) {
            if (telecom.getSystem() == ContactPoint.ContactPointSystem.PHONE) {
                dto.setPhone(telecom.getValue());
            } else if (telecom.getSystem() == ContactPoint.ContactPointSystem.EMAIL) {
                dto.setEmail(telecom.getValue());
            }
        }

        if (!fhirPatient.getAddress().isEmpty()) {
            dto.setAddress(fhirPatient.getAddressFirstRep().getText());
        }

        return dto;
    }
}
