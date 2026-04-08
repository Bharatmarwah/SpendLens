package com.bharat.SpendLens.service;


import com.bharat.SpendLens.entity.AuthUser;
import com.bharat.SpendLens.exception.ProfileAlreadyCompletedException;
import com.bharat.SpendLens.exception.ResourceNotFoundException;
import com.bharat.SpendLens.repository.AuthUserRepo;
import com.bharat.SpendLens.requestdto.ProfileRequestDTO;
import com.bharat.SpendLens.responsedto.ProfileResponseDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final AuthUserRepo authUserRepo;

    public void updateProfile(@Valid ProfileRequestDTO requestDTO){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();

        Long userIdLong = Long.parseLong(userId);

        AuthUser user = authUserRepo.findById(userIdLong)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isProfileCompleted()) {
            throw new ProfileAlreadyCompletedException("Profile already completed");
        }

        user.setName(requestDTO.getName());
        user.setEmail(requestDTO.getEmail());
        user.setProfileCompleted(true);

        authUserRepo.save(user);
    }


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
