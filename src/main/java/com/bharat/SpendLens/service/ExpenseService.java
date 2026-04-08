package com.bharat.SpendLens.service;

import com.bharat.SpendLens.entity.Expense;
import com.bharat.SpendLens.exception.ResourceNotFoundException;
import com.bharat.SpendLens.repository.ExpenseRepo;
import com.bharat.SpendLens.requestdto.ExpenseRequestDTO;
import com.bharat.SpendLens.responsedto.ExpensePageResponseDTO;
import com.bharat.SpendLens.responsedto.ExpenseResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepo expenseRepo;


    @Transactional
    public ExpenseResponseDTO addExpense(@Valid ExpenseRequestDTO requestDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userIdStr = authentication.getName();
        Long userId = Long.parseLong(userIdStr);

        Expense expense = new Expense();
        expense.setCategory(requestDTO.getCategory());
        expense.setDescription(requestDTO.getDescription());
        expense.setAmount(requestDTO.getAmount());
        expense.setCreatedAt(Instant.now());
        expense.setUpdatedAt(Instant.now());
        expense.setUserId(userId);

        expenseRepo.save(expense);

        return ExpenseResponseDTO
                .builder()
                .id(expense.getId())
                .category(expense.getCategory())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .createdAt(expense.getCreatedAt())
                .build();
    }

    @Transactional
    public ExpenseResponseDTO updateExpense(Long id,
                                            @Valid ExpenseRequestDTO requestDTO) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userIdStr = authentication.getName();
        Long userId = Long.parseLong(userIdStr);

        Expense expense = expenseRepo
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        if (requestDTO.getAmount() != null) {
            expense.setAmount(requestDTO.getAmount());
        }

        if (requestDTO.getCategory() != null) {
            expense.setCategory(requestDTO.getCategory());
        }

        if (requestDTO.getDescription() != null) {
            expense.setDescription(requestDTO.getDescription());
        }

        expense.setUpdatedAt(Instant.now());

        return ExpenseResponseDTO
                .builder()
                .id(expense.getId())
                .category(expense.getCategory())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .createdAt(expense.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public ExpensePageResponseDTO getExpenses(int page, int size, String category, Instant startDate, Instant endDate) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userIdStr = authentication.getName();
        Long userId = Long.parseLong(userIdStr);

        Page<Expense> expensePage = expenseRepo.findByUserIdAndFilters(userId, category, startDate, endDate, PageRequest.of(page, size));

        return ExpensePageResponseDTO
                .builder()
                .expenses(expensePage.getContent().stream().map(expense -> ExpenseResponseDTO
                        .builder()
                        .id(expense.getId())
                        .category(expense.getCategory())
                        .description(expense.getDescription())
                        .amount(expense.getAmount())
                        .createdAt(expense.getCreatedAt())
                        .build()).toList())
                .page(expensePage.getNumber())
                .totalElements(expensePage.getTotalElements())
                .totalPages(expensePage.getTotalPages())
                .build();
    }

    @Transactional
    public void deleteExpense(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userIdStr = authentication.getName();
        Long userId = Long.parseLong(userIdStr);

        Expense expense = expenseRepo
                .findByIdAndUserId(id, userId).orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        expenseRepo.delete(expense);
    }
}
