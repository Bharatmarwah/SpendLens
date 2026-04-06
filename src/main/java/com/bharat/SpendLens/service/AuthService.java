package com.bharat.SpendLens.service;

import com.bharat.SpendLens.entity.AuthUser;
import com.bharat.SpendLens.repository.AuthUserRepo;
import com.bharat.SpendLens.requestdto.SendOtpRequestDTO;
import com.bharat.SpendLens.requestdto.VerifyOtpRequestDTO;
import com.bharat.SpendLens.responsedto.VerifyOtpResponseDTO;
import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    public static final String TOKEN_TYPE = "Bearer";

    private final AuthUserRepo authUserRepo;

    private final JwtService jwtService;

    @Value("${twilio.account-sid}")
    private String serviceSid;

    @Value("${twilio.service-sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Async
    public void sendOtp(@Valid SendOtpRequestDTO requestDTO) {
        try {
            Twilio.init(accountSid, authToken);
            Verification verification = Verification.creator(
                    serviceSid,
                    requestDTO.getPhoneNumber(),
                    "sms"
            ).create();
        } catch (Exception e) {
            throw new RuntimeException("Error sending sms");
        }
    }

    @Transactional
    public VerifyOtpResponseDTO verifyOtp(@Valid VerifyOtpRequestDTO requestDTO,HttpServletResponse response) {

        VerificationCheck verificationCheck = VerificationCheck
                .creator(serviceSid)
                .setTo(requestDTO.getPhoneNumber())
                .setCode(requestDTO.getOtp())
                .create();

        if (!"approved".equalsIgnoreCase(verificationCheck.getStatus())) {
            throw new RuntimeException("Invalid OTP");
        }

        AuthUser user = authUserRepo.findByPhoneNumber(requestDTO.getPhoneNumber())
                .orElseGet(() -> {
                    AuthUser u = new AuthUser();
                    u.setPhoneNumber(requestDTO.getPhoneNumber());
                    u.setCreatedAt(Instant.now());
                    u.setProfileCompleted(false);
                    return authUserRepo.save(u);
                });


        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        addCookie(response,refreshToken);

        return VerifyOtpResponseDTO
                .builder()
                .tokenType(TOKEN_TYPE)
                .token(accessToken)
                .isProfileComplete(false)
                .build();

    }

    public TokenResponse refreshToken(HttpServletRequest request){
        String refreshToken = extractFromCookie(request);

    }

    //----HELPERS---------

    private void addCookie(HttpServletResponse response ,String refreshToken){
        Cookie cookie = new Cookie("refresh-token",refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 60 * 60);

        response.addCookie(cookie);
    }

    private String extractFromCookie(HttpServletRequest request){
        String response = null;
        Cookie[] cookies = request.getCookies();
        if (cookies==null){
            throw new RuntimeException("Cookie not found");
        }
        for (Cookie c : cookies){
            if ("refresh-token".equals(c.getName())){
               response = c.getValue();
            }
        }
        return response;
    }
}
