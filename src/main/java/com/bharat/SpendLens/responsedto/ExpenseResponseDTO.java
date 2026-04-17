package com.bharat.SpendLens.responsedto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
public class ExpenseResponseDTO {

    private Long id;
    private String category;
    private String description;
    private BigDecimal amount;
    private Instant createdAt;
    private Instant updatedAt;



}
