package com.bharat.SpendLens.responsedto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileResponseDTO {
    private String name;
    private String email;
    private String phoneNumber;

}
