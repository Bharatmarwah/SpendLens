package com.bharat.SpendLens.requestdto;

import lombok.Data;


import java.util.List;

@Data
public class DeleteExpensesRequestDTO {
    private List<Long> id;
}
