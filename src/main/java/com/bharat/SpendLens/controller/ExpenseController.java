package com.bharat.SpendLens.controller;

import com.bharat.SpendLens.requestdto.ExpenseRequestDTO;
import com.bharat.SpendLens.responsedto.ExpensePageResponseDTO;
import com.bharat.SpendLens.responsedto.ExpenseResponseDTO;
import com.bharat.SpendLens.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponseDTO> addExpense(@Valid @RequestBody ExpenseRequestDTO requestDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.addExpense(requestDTO));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> updateExpense(@PathVariable Long id, @Valid @RequestBody ExpenseRequestDTO requestDTO){
        return ResponseEntity.status(HttpStatus.OK).body(expenseService.updateExpense(id,requestDTO));
    }

    @GetMapping
    public ResponseEntity<ExpensePageResponseDTO> getExpenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate
    ) {

        return ResponseEntity.ok(
                expenseService.getExpenses(page, size, category, minAmount, maxAmount, startDate, endDate)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id){
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

}
