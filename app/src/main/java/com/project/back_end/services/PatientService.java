package com.project.back_end.services;

// PatientService.java
import java.util.List;
import com.project.back_end.models.Appointment;

public interface PatientService {
    List<Appointment> getAllAppointmentsForPatient(Long patientId);
    List<Appointment> filterAppointmentsForPatient(Long patientId, String conditionContains, String doctorNameContains);
}
