package com.bharat.SpendLens.service;

import com.bharat.SpendLens.client.ToolDecisionClient;
import com.bharat.SpendLens.exception.ExpenseProcessingException;
import com.bharat.SpendLens.exception.InvalidAmountException;
import com.bharat.SpendLens.exception.MissingFieldException;
import com.bharat.SpendLens.exception.ResourceNotFoundException;
import com.bharat.SpendLens.requestdto.AiRequest;
import com.bharat.SpendLens.requestdto.DeleteExpensesRequestDTO;
import com.bharat.SpendLens.requestdto.ExpenseRequestDTO;
import com.bharat.SpendLens.responsedto.AiResponse;
import com.bharat.SpendLens.responsedto.ExpenseResponseDTO;
import com.bharat.SpendLens.responsedto.ToolMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrchestratorService {

    private final ToolDecisionClient toolDecisionClient;
    private final ExpenseService expenseService;
    private final DateZone dateZone;

    public AiResponse askAgent(AiRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        request.setUserId(userId);

        ToolMessageResponse response =
                toolDecisionClient.getToolExtraction(request);

        if (response.getType().equals("tool_call")) {

            if(response.getToolName().equals("add_expense")) {
                return handleAddExpense(response.getArguments());
            }
            if (response.getToolName().equals("update_expense")) {
                return handleUpdateExpense(response.getArguments());
            }
            if (response.getToolName().equals("delete_expense")) {
                return handleDeleteExpense(response.getArguments());
            }
            if (response.getToolName().equals("get_summary_report")){
                return handleExpenseReport(response.getArguments());
            }
        }

        return new AiResponse(response.getMessage());
    }

    private AiResponse handleExpenseReport(Map<String, Object> args) {
        try {
            // Extract parameters with null-safe handling
            Long expenseId = null;
            if (args.get("expense_id") != null) {
                try {
                    expenseId = Long.parseLong(args.get("expense_id").toString());
                } catch (NumberFormatException e) {
                    log.warn("Invalid expense_id format: {}", args.get("expense_id"));
                }
            }

            String category = args.get("category") != null
                    ? args.get("category").toString().toUpperCase().trim()
                    : null;

            BigDecimal minAmount = null;
            if (args.get("min_amount") != null) {
                try {
                    minAmount = new BigDecimal(args.get("min_amount").toString());
                } catch (NumberFormatException e) {
                    log.warn("Invalid min_amount format: {}", args.get("min_amount"));
                }
            }

            BigDecimal maxAmount = null;
            if (args.get("max_amount") != null) {
                try {
                    maxAmount = new BigDecimal(args.get("max_amount").toString());
                } catch (NumberFormatException e) {
                    log.warn("Invalid max_amount format: {}", args.get("max_amount"));
                }
            }

            // ✅ KEY: Convert IST dates to UTC date range for database query
            Instant startDate = null;
            if (args.get("start_date") != null) {
                try {
                    // Convert IST start of day to UTC
                    startDate = dateZone.convertToInstant(args.get("start_date").toString());
                    log.info("Start date (IST) {} converted to UTC: {}", args.get("start_date"), startDate);
                } catch (Exception e) {
                    log.warn("Invalid start_date format: {}", args.get("start_date"), e);
                }
            }

            Instant endDate = null;
            if (args.get("end_date") != null) {
                try {
                    // Convert IST end of day to UTC
                    endDate = dateZone.convertToInstantEndOfDay(args.get("end_date").toString());
                    log.info("End date (IST) {} converted to UTC: {}", args.get("end_date"), endDate);
                } catch (Exception e) {
                    log.warn("Invalid end_date format: {}", args.get("end_date"), e);
                }
            }

            log.info("Generating expense report: expenseId={}, category={}, minAmount={}, maxAmount={}, startDate={}, endDate={}",
                    expenseId, category, minAmount, maxAmount, startDate, endDate);

            // Call service with null values (handled by query IS NULL checks)
            List<ExpenseResponseDTO> allExpenseForUser = expenseService.getAllExpenseForUser(
                    expenseId, category, minAmount, maxAmount, startDate, endDate
            );

            if (allExpenseForUser == null || allExpenseForUser.isEmpty()) {
                log.info("No expenses found for the given filters");
                return new AiResponse("No expenses found matching your criteria.");
            }

            log.info("Found {} expenses", allExpenseForUser.size());
            return new AiResponse("Here is your expense report with " + allExpenseForUser.size() + " expenses matching the criteria.");

        } catch (Exception e) {
            log.error("Error generating expense report: {}", e.getMessage(), e);
            throw new ExpenseProcessingException("Failed to generate expense report: " + e.getMessage(), e);
        }
    }


    private AiResponse handleAddExpense(Map<String, Object> args) {

        try {
            Object amountObj = args.get("amount");
            Object categoryObj = args.get("category");

            if (amountObj == null) {
                log.error("Amount is missing from add_expense arguments");
                throw new MissingFieldException("Missing required field");
            }

            if (categoryObj == null) {
                log.error("Category is missing from add_expense arguments");
                throw new MissingFieldException("Missing required field");
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(amountObj.toString());

                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    log.error("Invalid amount: amount must be > 0, got {}", amount);
                    throw new InvalidAmountException("Amount must be greater than 0");
                }

            } catch (NumberFormatException e) {
                log.error("Invalid amount format: {}", amountObj, e);
                throw new InvalidAmountException("Invalid amount: " + amountObj, e);
            }

            String category = categoryObj.toString().toUpperCase().trim();
            if (category.isEmpty()) {
                log.error("Category is empty");
                throw new MissingFieldException("Category cannot be empty");
            }

            String description = args.get("description") != null
                    ? args.get("description").toString()
                    : null;

            log.info("Creating expense: amount={}, category={}, description={}", amount, category, description);

            ExpenseRequestDTO dto = new ExpenseRequestDTO();
            dto.setAmount(amount);
            dto.setCategory(category);
            dto.setDescription(description);

            expenseService.addExpense(dto);

            String response = "Expense of ₹" + amount + " added under " + category;
            log.info("Expense added successfully: {}", response);

            return new AiResponse(response);

        } catch (MissingFieldException | InvalidAmountException e) {
            log.error("Add expense validation error", e);
            throw e;
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found while adding expense", e);
            throw new ExpenseProcessingException("Failed to add expense: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error handling add_expense", e);
            throw new ExpenseProcessingException("Failed to add expense: " + e.getMessage(), e);
        }
    }

    private AiResponse handleUpdateExpense(Map<String, Object> args) {

        try {
            Long id = args.get("id") != null
                    ? Long.parseLong(args.get("id").toString())
                    : null;

            if (id == null) {
                throw new MissingFieldException("Missing required field: id");
            }

            BigDecimal amount = args.get("amount") != null
                    ? new BigDecimal(args.get("amount").toString())
                    : null;

            String category = args.get("category") != null
                    ? args.get("category").toString().toUpperCase().trim()
                    : null;

            String description = args.get("description") != null
                    ? args.get("description").toString()
                    : null;

            ExpenseRequestDTO dto = new ExpenseRequestDTO(amount, category, description);

            ExpenseResponseDTO updated = expenseService.updateExpense(id, dto);

            String response = "Expense ID " + id + " updated successfully";

            return new AiResponse(response);

        } catch (MissingFieldException e) {
            log.error("Update expense validation error", e);
            throw e;
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found while updating expense", e);
            throw new ExpenseProcessingException("Failed to update expense: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error handling update_expense", e);
            throw new ExpenseProcessingException("Failed to update expense: " + e.getMessage(), e);
        }
    }

    private AiResponse handleDeleteExpense(Map<String, Object> args) {

        try {
            Object idsObj = args.get("ids");

            if (idsObj == null) {
                throw new MissingFieldException("Missing required field: ids");
            }

            List<Long> ids = (List<Long>) idsObj;

            if (ids == null || ids.isEmpty()) {
                throw new MissingFieldException("Ids list cannot be empty");
            }

            DeleteExpensesRequestDTO requestDTO = new DeleteExpensesRequestDTO();
            requestDTO.setId(ids);

            // Get the actual response with deleted count
            com.bharat.SpendLens.responsedto.DeleteResponse deleteResponse =
                    expenseService.deleteMultipleExpenses(requestDTO);

            // Use the actual deleted count, not the requested count
            String response;
            int deletedCount = deleteResponse.getDeletedCount();
            int requestedCount = ids.size();
            String expenseWord = deletedCount == 1 ? "expense" : "expenses";
            String requestedWord = requestedCount == 1 ? "expense" : "expenses";

            if (deletedCount == requestedCount) {
                response = "Successfully deleted " + deletedCount + " " + expenseWord;
            } else {
                int notFound = requestedCount - deletedCount;
                String notFoundWord = notFound == 1 ? "expense" : "expenses";
                response = "Deleted " + deletedCount + " out of " + requestedCount +
                        " requested " + requestedWord + ". " + notFound +
                        " " + notFoundWord + " were not found.";
            }

            log.info("Expenses deleted: {}", response);

            return new AiResponse(response);

        } catch (MissingFieldException e) {
            log.error("Delete expense validation error", e);
            throw e;
        } catch (ResourceNotFoundException e) {
            // Return a friendly message instead of throwing exception
            log.warn("Some expenses not found for deletion: {}", e.getMessage());
            return new AiResponse("Some expenses were not found. They may have already been deleted.");
        } catch (Exception e) {
            log.error("Error handling delete_expense", e);
            throw new ExpenseProcessingException("Failed to delete expense(s): " + e.getMessage(), e);
        }

    }


}
