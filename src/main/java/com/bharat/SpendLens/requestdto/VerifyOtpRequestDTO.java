package com.bharat.SpendLens.requestdto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyOtpRequestDTO {

    @Pattern(
            regexp = "^\\+?[1-9]\\d{1,14}$",
            message = "Invalid phone number (use E.164 format: +{country}{number})"
    )
    private String phoneNumber;

    @Pattern(
            regexp = "^[0-9]{6}$",
            message = "OTP must be exactly 6 digits"
    )
    private String otp;


}
