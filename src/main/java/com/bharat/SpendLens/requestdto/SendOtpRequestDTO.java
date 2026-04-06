package com.bharat.SpendLens.requestdto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SendOtpRequestDTO {

    @Pattern(regexp = "^[0-9]{10}$",message = "Invalid phone number")
    private String phoneNumber;


}
