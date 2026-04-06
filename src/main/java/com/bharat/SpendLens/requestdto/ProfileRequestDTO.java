package com.bharat.SpendLens.requestdto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileRequestDTO {

    @NotBlank
    private String name;

    @Email
    private String email;
}
