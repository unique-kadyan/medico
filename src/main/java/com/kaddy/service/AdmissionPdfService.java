package com.kaddy.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.Hospital;
import com.kaddy.model.PatientAdmission;
import com.kaddy.repository.PatientAdmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdmissionPdfService {

    private final PatientAdmissionRepository admissionRepository;
    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(41, 128, 185);
    private static final DeviceRgb SECTION_COLOR = new DeviceRgb(236, 240, 241);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public byte[] generateAdmissionForm(Long admissionId) throws IOException {
        PatientAdmission admission = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found"));

        Hospital hospital = admission.getHospital();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(30, 30, 30, 30);

        try {
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            addHospitalHeader(document, hospital, boldFont, regularFont);

            document.add(new Paragraph("PATIENT ADMISSION FORM").setFont(boldFont).setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(10).setMarginBottom(10));

            Table admissionInfo = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .setWidth(UnitValue.createPercentValue(100));
            admissionInfo.addCell(createInfoCell("Admission No: " + admission.getAdmissionNumber(), boldFont));
            admissionInfo.addCell(
                    createInfoCell("Date: " + admission.getAdmissionDateTime().format(DATETIME_FORMAT), boldFont)
                            .setTextAlignment(TextAlignment.RIGHT));
            document.add(admissionInfo);

            addSectionHeader(document, "PATIENT INFORMATION", boldFont);
            addPatientInformation(document, admission, boldFont, regularFont);

            addSectionHeader(document, "MEDICAL INFORMATION", boldFont);
            addMedicalInformation(document, admission, boldFont, regularFont);

            addSectionHeader(document, "ADMISSION DETAILS", boldFont);
            addAdmissionDetails(document, admission, boldFont, regularFont);

            addSectionHeader(document, "EMERGENCY CONTACT", boldFont);
            addEmergencyContact(document, admission, boldFont, regularFont);

            if (admission.getHasInsurance() != null && admission.getHasInsurance()) {
                addSectionHeader(document, "INSURANCE INFORMATION", boldFont);
                addInsuranceInformation(document, admission, boldFont, regularFont);
            }

            addSectionHeader(document, "FINANCIAL INFORMATION", boldFont);
            addFinancialInformation(document, admission, boldFont, regularFont);

            addSignatureSection(document, admission, boldFont, regularFont);

            addFooter(document, hospital, regularFont);

            document.close();
        } catch (IOException e) {
            log.error("Error generating PDF", e);
            throw e;
        }

        return baos.toByteArray();
    }

    private void addHospitalHeader(Document document, Hospital hospital, PdfFont boldFont, PdfFont regularFont) {
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1}))
                .setWidth(UnitValue.createPercentValue(100)).setBackgroundColor(HEADER_COLOR)
                .setFontColor(ColorConstants.WHITE);

        Cell headerCell = new Cell().setBorder(Border.NO_BORDER).setPadding(15);

        headerCell.add(new Paragraph(hospital.getName()).setFont(boldFont).setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER));

        if (hospital.getAddress() != null) {
            headerCell.add(new Paragraph(hospital.getAddress()).setFont(regularFont).setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        StringBuilder contactInfo = new StringBuilder();
        if (hospital.getPhone() != null) {
            contactInfo.append("Phone: ").append(hospital.getPhone());
        }
        if (hospital.getEmail() != null) {
            if (contactInfo.length() > 0)
                contactInfo.append(" | ");
            contactInfo.append("Email: ").append(hospital.getEmail());
        }
        if (contactInfo.length() > 0) {
            headerCell.add(new Paragraph(contactInfo.toString()).setFont(regularFont).setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        headerTable.addCell(headerCell);
        document.add(headerTable);
    }

    private void addSectionHeader(Document document, String title, PdfFont boldFont) {
        document.add(new Paragraph(title).setFont(boldFont).setFontSize(12).setBackgroundColor(SECTION_COLOR)
                .setPadding(5).setMarginTop(15).setMarginBottom(5));
    }

    private void addPatientInformation(Document document, PatientAdmission admission, PdfFont boldFont,
            PdfFont regularFont) {
        Table table = createInfoTable();

        addTableRow(table, "Patient Name",
                admission.getPatient().getFirstName() + " " + admission.getPatient().getLastName(), boldFont,
                regularFont);
        addTableRow(table, "Date of Birth",
                admission.getPatient().getDateOfBirth() != null
                        ? admission.getPatient().getDateOfBirth().format(DATE_FORMAT)
                        : "N/A",
                boldFont, regularFont);
        addTableRow(table, "Gender",
                admission.getPatient().getGender() != null ? admission.getPatient().getGender().toString() : "N/A",
                boldFont, regularFont);
        addTableRow(table, "Blood Group", admission.getBloodGroup() != null ? admission.getBloodGroup() : "N/A",
                boldFont, regularFont);
        addTableRow(table, "Phone",
                admission.getPatient().getPhone() != null ? admission.getPatient().getPhone() : "N/A", boldFont,
                regularFont);
        addTableRow(table, "Email",
                admission.getPatient().getEmail() != null ? admission.getPatient().getEmail() : "N/A", boldFont,
                regularFont);
        addTableRow(table, "Address",
                admission.getPatient().getAddress() != null ? admission.getPatient().getAddress() : "N/A", boldFont,
                regularFont);

        document.add(table);
    }

    private void addMedicalInformation(Document document, PatientAdmission admission, PdfFont boldFont,
            PdfFont regularFont) {
        Table table = createInfoTable();

        addTableRow(table, "Chief Complaint",
                admission.getChiefComplaint() != null ? admission.getChiefComplaint() : "N/A", boldFont, regularFont);
        addTableRow(table, "Admission Diagnosis",
                admission.getAdmissionDiagnosis() != null ? admission.getAdmissionDiagnosis() : "N/A", boldFont,
                regularFont);
        addTableRow(table, "Allergies", admission.getAllergies() != null ? admission.getAllergies() : "None known",
                boldFont, regularFont);
        addTableRow(table, "Current Medications",
                admission.getCurrentMedications() != null ? admission.getCurrentMedications() : "None", boldFont,
                regularFont);
        addTableRow(table, "Medical History",
                admission.getMedicalHistory() != null ? admission.getMedicalHistory() : "N/A", boldFont, regularFont);
        addTableRow(table, "Surgical History",
                admission.getSurgicalHistory() != null ? admission.getSurgicalHistory() : "N/A", boldFont, regularFont);
        addTableRow(table, "Vital Signs", admission.getVitalSigns() != null ? admission.getVitalSigns() : "N/A",
                boldFont, regularFont);

        document.add(table);
    }

    private void addAdmissionDetails(Document document, PatientAdmission admission, PdfFont boldFont,
            PdfFont regularFont) {
        Table table = createInfoTable();

        addTableRow(table, "Admission Type", admission.getAdmissionType().name(), boldFont, regularFont);
        addTableRow(table, "Ward", admission.getWard() != null ? admission.getWard().getName() : "N/A", boldFont,
                regularFont);
        addTableRow(table, "Bed Number", admission.getBed() != null ? admission.getBed().getBedNumber() : "N/A",
                boldFont, regularFont);
        addTableRow(table, "Admitting Doctor",
                admission.getAdmittingDoctor() != null
                        ? "Dr. " + admission.getAdmittingDoctor().getUser().getFirstName() + " "
                                + admission.getAdmittingDoctor().getUser().getLastName()
                        : "N/A",
                boldFont, regularFont);
        addTableRow(table, "Attending Doctor",
                admission.getAttendingDoctor() != null
                        ? "Dr. " + admission.getAttendingDoctor().getUser().getFirstName() + " "
                                + admission.getAttendingDoctor().getUser().getLastName()
                        : "N/A",
                boldFont, regularFont);
        addTableRow(table, "Expected Discharge",
                admission.getExpectedDischargeDate() != null
                        ? admission.getExpectedDischargeDate().format(DATE_FORMAT)
                        : "N/A",
                boldFont, regularFont);
        addTableRow(table, "Emergency Case", admission.getIsEmergency() ? "Yes" : "No", boldFont, regularFont);
        addTableRow(table, "Requires ICU", admission.getRequiresIcu() ? "Yes" : "No", boldFont, regularFont);

        if (admission.getTreatmentPlan() != null) {
            addTableRow(table, "Treatment Plan", admission.getTreatmentPlan(), boldFont, regularFont);
        }
        if (admission.getSpecialInstructions() != null) {
            addTableRow(table, "Special Instructions", admission.getSpecialInstructions(), boldFont, regularFont);
        }

        document.add(table);
    }

    private void addEmergencyContact(Document document, PatientAdmission admission, PdfFont boldFont,
            PdfFont regularFont) {
        Table table = createInfoTable();

        addTableRow(table, "Contact Name",
                admission.getEmergencyContactName() != null ? admission.getEmergencyContactName() : "N/A", boldFont,
                regularFont);
        addTableRow(table, "Contact Phone",
                admission.getEmergencyContactPhone() != null ? admission.getEmergencyContactPhone() : "N/A", boldFont,
                regularFont);
        addTableRow(table, "Relationship",
                admission.getEmergencyContactRelation() != null ? admission.getEmergencyContactRelation() : "N/A",
                boldFont, regularFont);

        document.add(table);
    }

    private void addInsuranceInformation(Document document, PatientAdmission admission, PdfFont boldFont,
            PdfFont regularFont) {
        Table table = createInfoTable();

        addTableRow(table, "Insurance Provider",
                admission.getInsuranceProvider() != null ? admission.getInsuranceProvider() : "N/A", boldFont,
                regularFont);
        addTableRow(table, "Policy Number",
                admission.getInsurancePolicyNumber() != null ? admission.getInsurancePolicyNumber() : "N/A", boldFont,
                regularFont);

        document.add(table);
    }

    private void addFinancialInformation(Document document, PatientAdmission admission, PdfFont boldFont,
            PdfFont regularFont) {
        Table table = createInfoTable();

        addTableRow(table, "Estimated Cost",
                admission.getEstimatedCost() != null ? "₹ " + admission.getEstimatedCost().toString() : "N/A", boldFont,
                regularFont);
        addTableRow(table, "Deposit Amount",
                admission.getDepositAmount() != null ? "₹ " + admission.getDepositAmount().toString() : "N/A", boldFont,
                regularFont);

        document.add(table);
    }

    private void addSignatureSection(Document document, PatientAdmission admission, PdfFont boldFont,
            PdfFont regularFont) {
        document.add(new Paragraph("\n"));

        Table signTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100)).setMarginTop(30);

        Cell patientSig = new Cell().setBorder(Border.NO_BORDER)
                .add(new Paragraph("_____________________").setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("Patient/Guardian Signature").setFont(regularFont).setFontSize(9)
                        .setTextAlignment(TextAlignment.CENTER));
        signTable.addCell(patientSig);

        Cell admittedBy = new Cell().setBorder(Border.NO_BORDER)
                .add(new Paragraph("_____________________").setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("Admitted By: " + (admission.getAdmittedBy() != null
                        ? admission.getAdmittedBy().getFirstName() + " " + admission.getAdmittedBy().getLastName()
                        : "")).setFont(regularFont).setFontSize(9).setTextAlignment(TextAlignment.CENTER));
        signTable.addCell(admittedBy);

        Cell doctorSig = new Cell().setBorder(Border.NO_BORDER)
                .add(new Paragraph("_____________________").setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("Doctor's Signature").setFont(regularFont).setFontSize(9)
                        .setTextAlignment(TextAlignment.CENTER));
        signTable.addCell(doctorSig);

        document.add(signTable);
    }

    private void addFooter(Document document, Hospital hospital, PdfFont regularFont) {
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("This is a computer-generated document. For official use only.").setFont(regularFont)
                .setFontSize(8).setTextAlignment(TextAlignment.CENTER).setFontColor(ColorConstants.GRAY));

        document.add(new Paragraph("Generated on: " + java.time.LocalDateTime.now().format(DATETIME_FORMAT))
                .setFont(regularFont).setFontSize(8).setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY));
    }

    private Table createInfoTable() {
        return new Table(UnitValue.createPercentArray(new float[]{1, 2})).setWidth(UnitValue.createPercentValue(100));
    }

    private void addTableRow(Table table, String label, String value, PdfFont boldFont, PdfFont regularFont) {
        table.addCell(new Cell().setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f)).setPadding(5)
                .add(new Paragraph(label).setFont(boldFont).setFontSize(10)));

        table.addCell(new Cell().setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f)).setPadding(5)
                .add(new Paragraph(value != null ? value : "N/A").setFont(regularFont).setFontSize(10)));
    }

    private Cell createInfoCell(String text, PdfFont font) {
        return new Cell().setBorder(Border.NO_BORDER).add(new Paragraph(text).setFont(font).setFontSize(10));
    }
}
