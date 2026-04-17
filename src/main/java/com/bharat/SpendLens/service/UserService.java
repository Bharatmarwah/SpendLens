package com.bharat.SpendLens.service;

import com.bharat.SpendLens.entity.AuthUser;
import com.bharat.SpendLens.exception.ResourceNotFoundException;
import com.bharat.SpendLens.repository.AuthUserRepo;
import com.bharat.SpendLens.requestdto.ProfileRequestDTO;
import com.bharat.SpendLens.responsedto.ProfileResponseDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserService {

    private final AuthUserRepo authUserRepo;

    @Transactional
    public void updateProfile(@Valid ProfileRequestDTO requestDTO){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();

        Long userIdLong = Long.parseLong(userId);

        AuthUser user = authUserRepo.findById(userIdLong)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (requestDTO.getName()==null||requestDTO.getName().isBlank()){
            throw new IllegalArgumentException("Name cannot be null or blank");
        }

        if (requestDTO.getEmail()==null||requestDTO.getEmail().isBlank()){
            throw new IllegalArgumentException("Email cannot be null or blank");
        }

        user.setName(requestDTO.getName());
        user.setEmail(requestDTO.getEmail());


        if (!user.isProfileCompleted()) {
            user.setProfileCompleted(true);
        }

        authUserRepo.save(user);
    }


    @Transactional(readOnly = true)
    public ProfileResponseDTO getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();

        Long userIdLong = Long.parseLong(userId);

        AuthUser user = authUserRepo
                .findById(userIdLong)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return ProfileResponseDTO
                .builder()
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
