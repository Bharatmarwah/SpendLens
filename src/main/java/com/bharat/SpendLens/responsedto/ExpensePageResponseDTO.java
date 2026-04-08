package com.bharat.SpendLens.responsedto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ExpensePageResponseDTO {

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    private List<ExpenseResponseDTO> expenses;

}
