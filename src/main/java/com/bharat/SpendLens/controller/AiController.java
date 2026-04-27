package com.bharat.SpendLens.controller;

import com.bharat.SpendLens.requestdto.AiRequest;
import com.bharat.SpendLens.requestdto.DeleteExpensesRequestDTO;
import com.bharat.SpendLens.responsedto.AiResponse;
import com.bharat.SpendLens.responsedto.DeleteResponse;
import com.bharat.SpendLens.responsedto.ExpenseResponseDTO;
import com.bharat.SpendLens.service.ExpenseService;
import com.bharat.SpendLens.service.OrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final ExpenseService expenseService;
    private final OrchestratorService orchestratorService;


    @PostMapping("/ask")
    public ResponseEntity<AiResponse> askAgent(@RequestBody AiRequest request) {
        return ResponseEntity.ok(orchestratorService.askAgent(request));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ExpenseResponseDTO>> getExpensesForUser(
            @RequestParam(required = false) Long expenseId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(expenseService.getAllExpenseForUser(expenseId, category, minAmount, maxAmount, startDate, endDate));

    }

    @DeleteMapping("/delete")
    public ResponseEntity<DeleteResponse> deleteMultipleExpenses(@RequestBody DeleteExpensesRequestDTO requestDTO){
        return ResponseEntity.status(HttpStatus.OK).body(expenseService.deleteMultipleExpenses(requestDTO));
    }
}
