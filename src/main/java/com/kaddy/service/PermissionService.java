package com.kaddy.service;

import com.kaddy.model.enums.UserRole;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PermissionService {

    public List<String> getPermissionsForRole(UserRole role) {
        List<String> permissions = new ArrayList<>();

        switch (role) {
            case ADMIN :
                permissions.addAll(getAllPermissions());
                break;

            case DOCTOR :
                permissions.add("VIEW_ASSIGNED_PATIENTS");
                permissions.add("VIEW_PATIENT_DETAILS");
                permissions.add("VIEW_PATIENT_REPORTS");
                permissions.add("ADD_FOLLOW_UP");
                permissions.add("VIEW_FOLLOW_UPS");
                permissions.add("PRESCRIBE_MEDICATION");
                permissions.add("VIEW_MEDICATIONS");
                permissions.add("VIEW_APPOINTMENTS");
                permissions.add("VIEW_LAB_TESTS");
                permissions.add("ORDER_LAB_TEST");
                permissions.add("VIEW_DOCTORS");
                permissions.add("VIEW_PROFILE");
                break;

            case NURSE :
                permissions.add("VIEW_ASSIGNED_PATIENTS");
                permissions.add("VIEW_PATIENT_DETAILS");
                permissions.add("VIEW_PATIENT_REPORTS");
                permissions.add("ADD_FOLLOW_UP");
                permissions.add("VIEW_FOLLOW_UPS");
                permissions.add("VIEW_APPOINTMENTS");
                permissions.add("VIEW_LAB_TESTS");
                permissions.add("ORDER_LAB_TEST");
                permissions.add("VIEW_DOCTORS");
                permissions.add("VIEW_PROFILE");
                break;

            case RECEPTIONIST :
                permissions.add("VIEW_ALL_PATIENTS");
                permissions.add("ADD_PATIENT");
                permissions.add("EDIT_PATIENT");
                permissions.add("VIEW_PATIENT_DETAILS");
                permissions.add("VIEW_APPOINTMENTS");
                permissions.add("BOOK_APPOINTMENT");
                permissions.add("VIEW_DOCTORS");
                permissions.add("VIEW_PROFILE");
                break;

            case LAB_TECHNICIAN :
                permissions.add("VIEW_LAB_TESTS");
                permissions.add("UPLOAD_LAB_RESULTS");
                permissions.add("VIEW_PROFILE");
                break;

            case DOCTOR_SUPERVISOR :
                permissions.add("VIEW_ASSIGNED_PATIENTS");
                permissions.add("VIEW_ALL_PATIENTS");
                permissions.add("VIEW_PATIENT_DETAILS");
                permissions.add("VIEW_PATIENT_REPORTS");
                permissions.add("ADD_FOLLOW_UP");
                permissions.add("VIEW_FOLLOW_UPS");
                permissions.add("PRESCRIBE_MEDICATION");
                permissions.add("VIEW_MEDICATIONS");
                permissions.add("VIEW_APPOINTMENTS");
                permissions.add("VIEW_LAB_TESTS");
                permissions.add("ORDER_LAB_TEST");
                permissions.add("VIEW_DOCTORS");
                permissions.add("ADD_DOCTOR"); // Can add doctors (subject to admin approval)
                permissions.add("EDIT_DOCTOR");
                permissions.add("APPROVE_DOCTOR_REGISTRATION"); // New permission
                permissions.add("VIEW_PENDING_REGISTRATIONS"); // New permission
                permissions.add("VIEW_PROFILE");
                break;

            case NURSE_MANAGER :
            case NURSE_SUPERVISOR :
                permissions.add("VIEW_ASSIGNED_PATIENTS");
                permissions.add("VIEW_ALL_PATIENTS");
                permissions.add("VIEW_PATIENT_DETAILS");
                permissions.add("VIEW_PATIENT_REPORTS");
                permissions.add("ADD_FOLLOW_UP");
                permissions.add("VIEW_FOLLOW_UPS");
                permissions.add("VIEW_APPOINTMENTS");
                permissions.add("VIEW_LAB_TESTS");
                permissions.add("VIEW_DOCTORS");
                permissions.add("VIEW_NURSES");
                permissions.add("ADD_NURSE"); // Can add nurses (subject to approval)
                permissions.add("EDIT_NURSE");
                permissions.add("APPROVE_NURSE_REGISTRATION"); // New permission
                permissions.add("VIEW_PENDING_REGISTRATIONS"); // New permission
                permissions.add("VIEW_PROFILE");
                break;

            case PHARMACIST :
                permissions.add("VIEW_MEDICATIONS");
                permissions.add("ADD_MEDICATION");
                permissions.add("EDIT_MEDICATION");
                permissions.add("DELETE_MEDICATION");
                permissions.add("VIEW_MEDICATION_REQUESTS");
                permissions.add("APPROVE_MEDICATION_REQUEST");
                permissions.add("VIEW_PROFILE");
                break;

            case PATIENT :
                permissions.add("VIEW_OWN_PROFILE");
                permissions.add("VIEW_OWN_APPOINTMENTS");
                permissions.add("BOOK_APPOINTMENT");
                permissions.add("VIEW_OWN_LAB_TESTS");
                permissions.add("VIEW_OWN_PRESCRIPTIONS");
                permissions.add("VIEW_OWN_MEDICAL_RECORDS");
                permissions.add("VIEW_DOCTORS");
                break;

            default :
                break;
        }

        return permissions;
    }

    private List<String> getAllPermissions() {
        return List.of("VIEW_ALL_PATIENTS", "VIEW_ASSIGNED_PATIENTS", "ADD_PATIENT", "EDIT_PATIENT", "DELETE_PATIENT",
                "VIEW_PATIENT_DETAILS", "VIEW_PATIENT_REPORTS",

                "VIEW_DOCTORS", "ADD_DOCTOR", "EDIT_DOCTOR", "DELETE_DOCTOR",

                "VIEW_NURSES", "ADD_NURSE", "EDIT_NURSE", "DELETE_NURSE",

                "VIEW_LAB_TECHNICIANS", "ADD_LAB_TECHNICIAN", "EDIT_LAB_TECHNICIAN", "DELETE_LAB_TECHNICIAN",

                "VIEW_RECEPTIONISTS", "ADD_RECEPTIONIST", "EDIT_RECEPTIONIST", "DELETE_RECEPTIONIST",

                "VIEW_PHARMACISTS", "ADD_PHARMACIST", "EDIT_PHARMACIST", "DELETE_PHARMACIST",

                "ASSIGN_DOCTOR_TO_PATIENT", "REMOVE_DOCTOR_FROM_PATIENT", "VIEW_ASSIGNMENTS",

                "VIEW_APPOINTMENTS", "BOOK_APPOINTMENT", "CANCEL_APPOINTMENT", "RESCHEDULE_APPOINTMENT",

                "VIEW_MEDICATIONS", "ADD_MEDICATION", "EDIT_MEDICATION", "DELETE_MEDICATION", "PRESCRIBE_MEDICATION",

                "VIEW_MEDICATION_REQUESTS", "APPROVE_MEDICATION_REQUEST",

                "VIEW_FOLLOW_UPS", "ADD_FOLLOW_UP", "EDIT_FOLLOW_UP", "DELETE_FOLLOW_UP",

                "VIEW_LAB_TESTS", "ORDER_LAB_TEST", "UPLOAD_LAB_RESULTS", "VIEW_LAB_RESULTS",

                "VIEW_NOTIFICATIONS",

                "VIEW_MONITORING", "VIEW_SYSTEM_STATS",

                "VIEW_PROFILE", "EDIT_PROFILE",

                "VIEW_OWN_PROFILE", "VIEW_OWN_APPOINTMENTS", "VIEW_OWN_LAB_TESTS", "VIEW_OWN_PRESCRIPTIONS",
                "VIEW_OWN_MEDICAL_RECORDS",

                "VIEW_PENDING_REGISTRATIONS", "APPROVE_DOCTOR_REGISTRATION", "APPROVE_NURSE_REGISTRATION",
                "APPROVE_RECEPTIONIST_REGISTRATION", "APPROVE_SUPERVISOR_REGISTRATION", "REJECT_REGISTRATION");
    }
}
