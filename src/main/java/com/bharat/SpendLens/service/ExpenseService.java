package com.bharat.SpendLens.service;

import com.bharat.SpendLens.entity.Expense;
import com.bharat.SpendLens.exception.ExpenseProcessingException;
import com.bharat.SpendLens.exception.ResourceNotFoundException;
import com.bharat.SpendLens.repository.ExpenseRepo;
import com.bharat.SpendLens.requestdto.DeleteExpensesRequestDTO;
import com.bharat.SpendLens.requestdto.ExpenseRequestDTO;
import com.bharat.SpendLens.responsedto.DeleteResponse;
import com.bharat.SpendLens.responsedto.ExpensePageResponseDTO;
import com.bharat.SpendLens.responsedto.ExpenseResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepo expenseRepo;

    @CacheEvict(value = {"expenses","expenses_list"},allEntries = true)
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
                .createdAt(ExpenseResponseDTO.formatToIndiaTime(expense.getCreatedAt()))
                .updatedAt(ExpenseResponseDTO.formatToIndiaTime(expense.getUpdatedAt()))
                .build();
    }

    @CacheEvict(cacheNames = {"expenses","expenses_list"},allEntries = true)
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
                .createdAt(ExpenseResponseDTO.formatToIndiaTime(expense.getCreatedAt()))
                .updatedAt(ExpenseResponseDTO.formatToIndiaTime(expense.getUpdatedAt()))
                .build();
    }

    @Cacheable(
            value = "expenses",
            key = "#root.methodName + '_' + #page + '_' + #size + '_' + #category + '_' + #minAmount + '_' + #maxAmount + '_' + #startDate + '_' + #endDate + '_' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().authentication.name"
    )
    @Transactional(readOnly = true)
    public ExpensePageResponseDTO getExpenses(int page, int size, String category, BigDecimal minAmount, BigDecimal maxAmount, Instant startDate, Instant endDate) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userIdStr = authentication.getName();
        Long userId = Long.parseLong(userIdStr);

        Page<Expense> expensePage = expenseRepo.findByUserIdAndFilters(userId, category, minAmount, maxAmount ,startDate, endDate, PageRequest.of(page, size));

        return ExpensePageResponseDTO
                .builder()
                .expenses(expensePage.getContent().stream().map(expense -> ExpenseResponseDTO
                        .builder()
                        .id(expense.getId())
                        .category(expense.getCategory())
                        .description(expense.getDescription())
                        .amount(expense.getAmount())
                        .createdAt(ExpenseResponseDTO.formatToIndiaTime(expense.getCreatedAt()))
                        .updatedAt(ExpenseResponseDTO.formatToIndiaTime(expense.getUpdatedAt()))
                        .build()).toList())
                .page(expensePage.getNumber())
                .totalElements(expensePage.getTotalElements())
                .totalPages(expensePage.getTotalPages())
                .build();
    }

    @CacheEvict(cacheNames = {"expenses","expenses_list"},allEntries = true)
    @Transactional
    public void deleteExpense(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userIdStr = authentication.getName();
        Long userId = Long.parseLong(userIdStr);

        Expense expense = expenseRepo
                .findByIdAndUserId(id, userId).orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        expenseRepo.delete(expense);
    }

    @Cacheable(
            value = "expenses_list",
            key = "#expenseId + '_' + #category + '_' + #minAmount + '_' + #maxAmount + '_' + #startDate + '_' + #endDate + '_' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().authentication.name"
    )
    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getAllExpenseForUser(Long expenseId , String category, BigDecimal minAmount, BigDecimal maxAmount, Instant startDate, Instant endDate){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdStr = authentication.getName();
        Long userId = Long.parseLong(userIdStr);

        List<Expense> expenses = expenseRepo.findExpenseByFilter(userId, expenseId, category, minAmount, maxAmount, startDate, endDate);

        return expenses.stream().map(expense -> ExpenseResponseDTO
                .builder()
                .id(expense.getId())
                .category(expense.getCategory())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .createdAt(ExpenseResponseDTO.formatToIndiaTime(expense.getCreatedAt()))
                .updatedAt(ExpenseResponseDTO.formatToIndiaTime(expense.getUpdatedAt()))
                .build())
                .toList();
    }

    @CacheEvict(cacheNames = {"expenses","expenses_list"},allEntries = true)
    @Transactional
    public DeleteResponse deleteMultipleExpenses(DeleteExpensesRequestDTO requestDTO) {

        // Validation
        if (requestDTO.getId() == null || requestDTO.getId().isEmpty()) {
            throw new IllegalArgumentException("No expense IDs provided");
        }
        if (requestDTO.getId().size() > 100) {
            throw new IllegalArgumentException("Cannot delete more than 100 at once");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        List<Expense> expensesToDelete = expenseRepo.findAllByIdInAndUserId(
                requestDTO.getId(),
                userId
        );

        if (expensesToDelete.isEmpty()) {
            throw new ExpenseProcessingException("No expenses found to delete");
        }

        if (expensesToDelete.size() != requestDTO.getId().size()) {
            log.warn("Only {}/{} expenses found", expensesToDelete.size(), requestDTO.getId().size());
        }

        expenseRepo.deleteAll(expensesToDelete);
        log.info("Deleted {} expenses for user {}", expensesToDelete.size(), userId);

        return new DeleteResponse("success", expensesToDelete.size() + " deleted", expensesToDelete.size());
    }

}
