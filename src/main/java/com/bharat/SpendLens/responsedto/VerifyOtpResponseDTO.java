package com.bharat.SpendLens.responsedto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class VerifyOtpResponseDTO {

    private String tokenType;
    private String token;
    private boolean isProfileComplete;


}
