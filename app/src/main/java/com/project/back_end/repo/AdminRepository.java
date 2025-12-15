package com.project.back_end.repo;

// AdminRepository.java
import java.util.Optional;

public interface AdminRepository {
    Optional<com.project.back_end.models.Admin> findByUsername(String username);
}
