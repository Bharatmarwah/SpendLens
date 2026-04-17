package com.bharat.SpendLens.controller;

import com.bharat.SpendLens.entity.Expense;
import com.bharat.SpendLens.requestdto.AiRequest;
import com.bharat.SpendLens.responsedto.AiResponse;
import com.bharat.SpendLens.responsedto.ExpenseResponseDTO;
import com.bharat.SpendLens.service.AiService;
import com.bharat.SpendLens.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final ExpenseService expenseService;

    @PostMapping("/ask")
    public ResponseEntity<AiResponse> askAi(@RequestBody AiRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdStr = authentication.getName();
        request.setUserId(userIdStr);
        return ResponseEntity.ok(aiService.askAi(request));
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
}
