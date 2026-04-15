package com.bharat.SpendLens.requestdto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseRequestDTO {

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String category;

    private String description;

}
