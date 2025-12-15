package com.project.back_end.services;

// TokenService.java
public interface TokenService {
    boolean validate(String token);
    String generateTokenForUser(String subject, String role);
    String extractSubject(String token);
}
