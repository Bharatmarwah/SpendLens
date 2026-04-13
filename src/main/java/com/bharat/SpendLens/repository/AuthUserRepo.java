package com.bharat.SpendLens.repository;

import com.bharat.SpendLens.entity.AuthUser;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthUserRepo extends JpaRepository<AuthUser, Long> {
    Optional<AuthUser> findByPhoneNumber(@Pattern(regexp = "^\\+?[1-9]\\d{1,14}$",message = "Invalid phone number (use E.164 format)") String phoneNumber);
}
