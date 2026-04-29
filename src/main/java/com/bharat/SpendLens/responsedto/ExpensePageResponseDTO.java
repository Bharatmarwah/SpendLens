package com.bharat.SpendLens.responsedto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpensePageResponseDTO {

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    private List<ExpenseResponseDTO> expenses;

}
