package com.project.back_end.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Prescription document
 *
 * Represents a prescription stored in MongoDB.
 * Mapped to the "prescriptions" collection using Spring Data MongoDB.
 *
 * Validations:
 * - patientName: not null, length 3..100
 * - appointmentId: not null
 * - medication: not null, length 3..100
 * - dosage: not null
 * - doctorNotes: max length 200 (optional)
 */
@Document(collection = "prescriptions")
public class Prescription {

    // 1) Primary Key
    /**
     * Unique identifier for each prescription (MongoDB ObjectId as String).
     * Spring Data will map to/from ObjectId automatically when using String.
     */
    @Id
    private String id;

    // 2) Patient name
    /**
     * Name of the patient receiving the prescription.
     * Required and must be between 3 and 100 characters.
     */
    @NotNull
    @Size(min = 3, max = 100)
    private String patientName;

    // 3) Appointment id (link to SQL appointment record)
    /**
     * Associated appointment ID where the prescription was given.
     * Required.
     */
    @NotNull
    private Long appointmentId;

    // 4) Medication
    /**
     * Medication name.
     * Required and must be between 3 and 100 characters.
     */
    @NotNull
    @Size(min = 3, max = 100)
    private String medication;

    // 5) Dosage
    /**
     * Dosage information for the medication.
     * Required.
     */
    @NotNull
    private String dosage;

    // 6) Doctor notes
    /**
     * Optional additional notes/instructions from the doctor.
     * Max length 200 characters.
     */
    @Size(max = 200)
    private String doctorNotes;

    // 7) Constructors

    /** No-argument constructor required by frameworks. */
    public Prescription() {
    }

    /**
     * Convenience constructor to initialize required fields.
     *
     * @param patientName  patient name (3..100 chars)
     * @param medication   medication name (3..100 chars)
     * @param dosage       dosage info (not null)
     * @param doctorNotes  optional notes (<= 200 chars)
     * @param appointmentId associated appointment id (not null)
     */
    public Prescription(String patientName,
                        String medication,
                        String dosage,
                        String doctorNotes,
                        Long appointmentId) {
        this.patientName = patientName;
        this.medication = medication;
        this.dosage = dosage;
        this.doctorNotes = doctorNotes;
        this.appointmentId = appointmentId;
    }

    // 8) Getters and Setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public String getMedication() { return medication; }
    public void setMedication(String medication) { this.medication = medication; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getDoctorNotes() { return doctorNotes; }
    public void setDoctorNotes(String doctorNotes) { this.doctorNotes = doctorNotes; }

    @Override
    public String toString() {
        return "Prescription{" +
                "id='" + id + '\'' +
                ", patientName='" + patientName + '\'' +
                ", appointmentId=" + appointmentId +
                ", medication='" + medication + '\'' +
                ", dosage='" + dosage + '\'' +
                ", doctorNotes='" + doctorNotes + '\'' +
                "}";
    }
}
