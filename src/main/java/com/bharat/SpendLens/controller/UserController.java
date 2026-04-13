package com.bharat.SpendLens.controller;


import com.bharat.SpendLens.requestdto.ProfileRequestDTO;
import com.bharat.SpendLens.responsedto.ProfileResponseDTO;
import com.bharat.SpendLens.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/me")
    public ResponseEntity<Void> updateProfile(@Valid @RequestBody ProfileRequestDTO requestDTO){
        userService.updateProfile(requestDTO);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponseDTO> getProfile(){
        return ResponseEntity.ok(userService.getProfile());
    }



}
