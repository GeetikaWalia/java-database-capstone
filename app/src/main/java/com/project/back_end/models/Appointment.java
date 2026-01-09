package com.project.back_end.models;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Future;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Appointment entity
 *
 * Represents a scheduled appointment between a Doctor and a Patient.
 * Mapped to the "appointments" table via JPA/Hibernate.
 */
@Entity
@Table(name = "appointments")
public class Appointment {

   
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @NotNull
    private Doctor doctor;

    @ManyToOne
    @NotNull
    private Patient patient;

    @Future
    @NotNull
    private LocalDateTime appointmentTime;

    @NotNull
    private int status; // 0 = scheduled, 1 = completed

    // Default constructor
    public Appointment() {}

    // Parameterized constructor
    public Appointment(Doctor doctor, Patient patient, LocalDateTime appointmentTime, int status) {
        this.doctor = doctor;
        this.patient = patient;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public LocalDateTime getAppointmentTime() { return appointmentTime; }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    } 

    // Utility methods
    public LocalDateTime getEndTime() {
        return appointmentTime.plusHours(1);
    }

    public LocalDate getAppointmentDate() {
        return appointmentTime.toLocalDate();
    }

    public LocalTime getAppointmentTimeOnly() {
        return appointmentTime.toLocalTime();
    }

}
