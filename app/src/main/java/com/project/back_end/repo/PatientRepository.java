package com.project.back_end.repo;


// PatientRepository.java
import java.util.Optional;
public interface PatientRepository {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Optional<com.project.back_end.models.Patient> findByEmail(String email);
}
