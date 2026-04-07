package com.bharat.SpendLens.requestdto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SendOtpRequestDTO {

    @Pattern(
            regexp = "^\\+?[1-9]\\d{1,14}$",
            message = "Invalid phone number (use E.164 format: +{country}{number})"
    )
    private String phoneNumber;


}
