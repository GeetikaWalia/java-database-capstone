package com.project.back_end.repo;


// AppointmentRepository.java
import java.time.LocalDate;
import java.util.List;
public interface AppointmentRepository {
    List<com.project.back_end.models.Appointment> findByDoctorIdAndDate(Long doctorId, LocalDate date);
}
