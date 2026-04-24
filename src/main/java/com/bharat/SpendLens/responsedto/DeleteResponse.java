package com.bharat.SpendLens.responsedto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteResponse {
    private String status;
    private String message;
    private int deletedCount;
}

