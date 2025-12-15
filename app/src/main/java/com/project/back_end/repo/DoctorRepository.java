package com.project.back_end.repo;


// DoctorRepository.java
import java.util.Optional;
import java.util.List;
public interface DoctorRepository {
    Optional<com.project.back_end.models.Doctor> findById(Long id);
    List<com.project.back_end.models.Doctor> findAll();
}
