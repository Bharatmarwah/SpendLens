package com.bharat.SpendLens.controller;

import com.bharat.SpendLens.requestdto.SendOtpRequestDTO;
import com.bharat.SpendLens.requestdto.VerifyOtpRequestDTO;
import com.bharat.SpendLens.responsedto.TokenResponse;
import com.bharat.SpendLens.responsedto.VerifyOtpResponseDTO;
import com.bharat.SpendLens.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // sending otp api
    @PostMapping("/send/otp")
    public ResponseEntity<Void> sendOtp(@Valid @RequestBody SendOtpRequestDTO requestDTO) {
        authService.sendOtp(requestDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/resend/otp")
    public ResponseEntity<Void> resendOtp(@Valid @RequestBody SendOtpRequestDTO requestDTO) {
        authService.sendOtp(requestDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // verifying opt api
    @PostMapping("/verify/otp")
    public ResponseEntity<VerifyOtpResponseDTO> verifyOtp(@Valid @RequestBody VerifyOtpRequestDTO requestDTO, HttpServletResponse response){
        return ResponseEntity.status(HttpStatus.OK).body(authService.verifyOtp(requestDTO,response));
    }

    // refresh token api
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.OK).body(authService.refreshToken(request));
    }

    //logout api
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response){
        authService.logout(response);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


}
