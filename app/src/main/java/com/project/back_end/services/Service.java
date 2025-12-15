package com.project.back_end.services;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

// Validation
import javax.validation.constraints.NotNull;

// Java
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

// Project-specific domain & repositories (adjust these to your actual packages)
import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.models.Appointment;
import com.project.back_end.repositories.AdminRepository;
import com.project.back_end.repositories.DoctorRepository;
import com.project.back_end.repositories.PatientRepository;
import com.project.back_end.repositories.AppointmentRepository;

// Token and auxiliary services (adjust to your actual implementations)
import com.project.back_end.security.TokenService;
import com.project.back_end.services.PatientService;

// DTOs you might use (optional)
class AuthResponse {
    public final String token;
    public final String message;
    public AuthResponse(String token, String message) { this.token = token; this.message = message; }
}
class ErrorResponse {
    public final String error;
    public ErrorResponse(String error) { this.error = error; }
}

@Service // 1) Marks this class as a Spring service component
public class Service {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientService patientService;

    // 2) Constructor injection promotes testability & immutability
    public Service(TokenService tokenService,
                   AdminRepository adminRepository,
                   DoctorRepository doctorRepository,
                   PatientRepository patientRepository,
                   AppointmentRepository appointmentRepository,
                   PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientService = patientService;
    }

    // 3) validateToken: checks if a JWT token is valid for a specific user/role
    public ResponseEntity<?> validateToken(@NotNull String token) {
        try {
            boolean valid = tokenService.validate(token);
            if (!valid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Invalid or expired token"));
            }
            return ResponseEntity.ok(new AuthResponse(token, "Token is valid"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Token validation error: " + ex.getMessage()));
        }
    }

    // 4) validateAdmin: login flow for admin; returns JWT when credentials are valid
    public ResponseEntity<?> validateAdmin(@NotNull String username, @NotNull String passwordPlain) {
        try {
            Optional<Admin> maybeAdmin = adminRepository.findByUsername(username);
            if (maybeAdmin.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Admin not found"));
            }

            Admin admin = maybeAdmin.get();

            // ⚠️ IMPORTANT: Use hashed password comparison in production
            // e.g., passwordEncoder.matches(passwordPlain, admin.getPasswordHash())
            boolean passwordMatches = Objects.equals(passwordPlain, admin.getPassword());
            if (!passwordMatches) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Invalid password"));
            }

            String token = tokenService.generateTokenForUser(username, "ADMIN");
            return ResponseEntity.ok(new AuthResponse(token, "Login successful"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Admin login error: " + ex.getMessage()));
        }
    }

    // 5) filterDoctor: flexible filtering by name, specialty, and available time slots
    public List<Doctor> filterDoctor(String nameContains,
                                     String specialtyEquals,
                                     LocalTime desiredStart,
                                     LocalTime desiredEnd) {
        List<Doctor> all = doctorRepository.findAll(); // Or use query methods

        return all.stream()
                  .filter(d -> {
                      boolean ok = true;
                      if (nameContains != null && !nameContains.isBlank()) {
                          String fullName = (d.getName() != null ? d.getName() : d.getFullName());
                          ok &= (fullName != null) &&
                                fullName.toLowerCase().contains(nameContains.toLowerCase());
                      }
                      if (specialtyEquals != null && !specialtyEquals.isBlank()) {
                          ok &= specialtyEquals.equalsIgnoreCase(d.getSpecialty());
                      }
                      if (desiredStart != null && desiredEnd != null) {
                          // Assuming availableTimes: list of slots with start/end (LocalTime)
                          ok &= d.getAvailableTimes() != null &&
                                d.getAvailableTimes().stream().anyMatch(slot ->
                                    slot.getStartTime() != null &&
                                    slot.getEndTime() != null &&
                                    !slot.getEndTime().isBefore(desiredStart) &&
                                    !slot.getStartTime().isAfter(desiredEnd)
                                );
                      }
                      return ok;
                  })
                  .collect(Collectors.toList());
    }

    // 6) validateAppointment: checks if a requested appointment time is valid for a doctor
    // Returns: 1 = valid, 0 = invalid time, -1 = doctor does not exist
    public int validateAppointment(long doctorId, LocalDate date, LocalTime requestedStart) {
        Optional<Doctor> maybeDoctor = doctorRepository.findById(doctorId);
        if (maybeDoctor.isEmpty()) return -1;

        Doctor doctor = maybeDoctor.get();
        // Verify doctor has an availability slot matching the requested date/time
        boolean inAvailability = doctor.getAvailableTimes() != null &&
                doctor.getAvailableTimes().stream().anyMatch(slot ->
                        slot.getWeekday() != null &&
                        slot.getWeekday().name().equalsIgnoreCase(date.getDayOfWeek().name()) &&
                        slot.getStartTime() != null &&
                        slot.getEndTime() != null &&
                        !requestedStart.isBefore(slot.getStartTime()) &&
                        !requestedStart.isAfter(slot.getEndTime())
                );

        if (!inAvailability) return 0;

        // Prevent overlaps with existing appointments for the doctor at the requested time
        List<Appointment> existing = appointmentRepository
                .findByDoctorIdAndDate(doctorId, date); // Implement this repo method
        boolean clash = existing.stream().anyMatch(appt ->
                appt.getAppointmentTime() != null &&
                appt.getAppointmentTime().toLocalTime().equals(requestedStart)
        );

        return clash ? 0 : 1;
    }

    // 7) validatePatient: uniqueness by email or phone
    public boolean validatePatient(String email, String phone) {
        boolean emailExists = (email != null && !email.isBlank()) &&
                patientRepository.existsByEmail(email);
        boolean phoneExists = (phone != null && !phone.isBlank()) &&
                patientRepository.existsByPhone(phone);
        return !(emailExists || phoneExists);
    }

    // 8) validatePatientLogin: returns JWT for valid patient login
    public ResponseEntity<?> validatePatientLogin(@NotNull String email, @NotNull String passwordPlain) {
        try {
            Optional<Patient> maybePatient = patientRepository.findByEmail(email);
            if (maybePatient.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Patient not found"));
            }

            Patient patient = maybePatient.get();
            // ⚠️ Compare hashed passwords in production
            if (!Objects.equals(passwordPlain, patient.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Invalid password"));
            }

            String token = tokenService.generateTokenForUser(email, "PATIENT");
            return ResponseEntity.ok(new AuthResponse(token, "Login successful"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Patient login error: " + ex.getMessage()));
        }
    }

    // 9) filterPatient: filters a patient's appointment history by condition and doctor name
    // Token identifies patient; returns filtered appointments
    public ResponseEntity<?> filterPatient(@NotNull String token,
                                           String conditionContains,
                                           String doctorNameContains) {
        try {
            if (!tokenService.validate(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Invalid or expired token"));
                       }
            String patientEmail = tokenService.extractSubject(token);

            Optional<Patient> maybePatient = patientRepository.findByEmail(patientEmail);
            if (maybePatient.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Patient not found"));
            }
            Patient patient = maybePatient.get();

            // Delegate to PatientService (implement these methods)
            List<Appointment> result;
            if ((conditionContains == null || conditionContains.isBlank()) &&
                (doctorNameContains == null || doctorNameContains.isBlank())) {
                result = patientService.getAllAppointmentsForPatient(patient.getId());
            } else {
                result = patientService.filterAppointmentsForPatient(
                        patient.getId(), conditionContains, doctorNameContains);
            }
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Filtering error: " + ex.getMessage()));
        }
    }}
