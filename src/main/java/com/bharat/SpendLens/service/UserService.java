package com.bharat.SpendLens.service;


import com.bharat.SpendLens.entity.AuthUser;
import com.bharat.SpendLens.repository.AuthUserRepo;
import com.bharat.SpendLens.requestdto.ProfileRequestDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final AuthUserRepo authUserRepo;

    public void updateProfile(@Valid ProfileRequestDTO requestDTO){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) auth.getPrincipal();

        Long userId = Long.parseLong(jwt.getSubject());

        AuthUser user = authUserRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isProfileCompleted()) {
            throw new RuntimeException("Profile already completed");
        }

        user.setName(requestDTO.getName());
        user.setEmail(requestDTO.getEmail());
        user.setProfileCompleted(true);

        authUserRepo.save(user);
    }




}
