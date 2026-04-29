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

            Instant startDate = null;
            if (args.get("start_date") != null) {
                try {
                    startDate = dateZone.convertToInstant(args.get("start_date").toString());
                    log.info("Start date (IST) {} converted to UTC: {}", args.get("start_date"), startDate);
                } catch (Exception e) {
                    log.warn("Invalid start_date format: {}", args.get("start_date"), e);
                }
            }

            Instant endDate = null;
            if (args.get("end_date") != null) {
                try {
                    endDate = dateZone.convertToInstantEndOfDay(args.get("end_date").toString());
                    log.info("End date (IST) {} converted to UTC: {}", args.get("end_date"), endDate);
                } catch (Exception e) {
                    log.warn("Invalid end_date format: {}", args.get("end_date"), e);
                }
            }

            log.info("Generating expense report: expenseId={}, category={}, minAmount={}, maxAmount={}, startDate={}, endDate={}",
                    expenseId, category, minAmount, maxAmount, startDate, endDate);

            // Fetch expenses from database
            List<ExpenseResponseDTO> allExpenseForUser = expenseService.getAllExpenseForUser(
                    expenseId, category, minAmount, maxAmount, startDate, endDate
            );

            if (allExpenseForUser == null || allExpenseForUser.isEmpty()) {
                log.info("No expenses found for the given filters");
                return new AiResponse("No expenses found matching your criteria.");
            }

            log.info("Found {} expenses", allExpenseForUser.size());

            // Build detailed expense report
            String expenseReport = buildExpenseReport(allExpenseForUser);
            log.debug("Generated expense report: {}", expenseReport);

            // Call FastAPI to generate AI-powered summary and insights
            AiResponse aiAnalysis = callFastAPIForAnalysis(expenseReport);

            log.info("Expense report analysis completed successfully");
            return aiAnalysis;

        } catch (Exception e) {
            log.error("Error generating expense report: {}", e.getMessage(), e);
            throw new ExpenseProcessingException("Failed to generate expense report: " + e.getMessage(), e);
        }
    }

    /**
     * Build a structured expense report optimized for LLM analysis
     * Format: Grouped by category with statistics for better context
     */
    private String buildExpenseReport(List<ExpenseResponseDTO> expenses) {
        StringBuilder builder = new StringBuilder();

        // Summary statistics section (LLM needs this first)
        builder.append("📊 EXPENSE ANALYSIS REPORT\n");
        builder.append("=".repeat(70)).append("\n\n");

        // Calculate summary metrics
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal avgAmount = BigDecimal.ZERO;
        int expenseCount = expenses.size();

        for (ExpenseResponseDTO expense : expenses) {
            totalAmount = totalAmount.add(expense.getAmount());
        }

        if (expenseCount > 0) {
            avgAmount = totalAmount.divide(new BigDecimal(expenseCount), 2, java.math.RoundingMode.HALF_UP);
        }

        // Summary section - Context for LLM
        builder.append("SUMMARY METRICS:\n");
        builder.append("├─ Total Expenses: ").append(expenseCount).append("\n");
        builder.append("├─ Total Amount: ₹").append(totalAmount).append("\n");
        builder.append("├─ Average Per Expense: ₹").append(avgAmount).append("\n");
        builder.append("└─ Time Period: ").append(expenses.get(0).getCreatedAt())
                .append(" to ").append(expenses.get(expenseCount - 1).getCreatedAt()).append("\n\n");

        // Group expenses by category
        Map<String, List<ExpenseResponseDTO>> groupedByCategory = new java.util.LinkedHashMap<>();
        for (ExpenseResponseDTO expense : expenses) {
            groupedByCategory.computeIfAbsent(expense.getCategory(), k -> new java.util.ArrayList<>())
                    .add(expense);
        }

        // Category breakdown section
        builder.append("CATEGORY BREAKDOWN:\n");
        builder.append("-".repeat(70)).append("\n");

        for (Map.Entry<String, List<ExpenseResponseDTO>> entry : groupedByCategory.entrySet()) {
            String category = entry.getKey();
            List<ExpenseResponseDTO> categoryExpenses = entry.getValue();

            BigDecimal categoryTotal = BigDecimal.ZERO;
            for (ExpenseResponseDTO exp : categoryExpenses) {
                categoryTotal = categoryTotal.add(exp.getAmount());
            }

            BigDecimal percentage = totalAmount.compareTo(BigDecimal.ZERO) > 0
                    ? categoryTotal.divide(totalAmount, 2, java.math.RoundingMode.HALF_UP)
                            .multiply(new BigDecimal(100))
                    : BigDecimal.ZERO;

            builder.append("\n📌 ").append(category).append(" (").append(categoryExpenses.size())
                    .append(" transactions)\n");
            builder.append("   Total: ₹").append(categoryTotal).append(" (").append(percentage).append("%)\n");

            // List individual transactions in this category
            for (ExpenseResponseDTO expense : categoryExpenses) {
                builder.append("   • ₹").append(expense.getAmount())
                        .append(" - ").append(expense.getDescription() != null ? expense.getDescription() : "N/A")
                        .append(" (").append(expense.getCreatedAt()).append(")\n");
            }
        }

        builder.append("\n").append("=".repeat(70)).append("\n");

        return builder.toString();
    }

    /**
     * Call FastAPI generate-report endpoint for AI-powered analysis
     */
    private AiResponse callFastAPIForAnalysis(String expenseReport) {
        try {
            log.info("Calling FastAPI to analyze expense report");

            com.bharat.SpendLens.requestdto.AiReportRequest reportRequest =
                    new com.bharat.SpendLens.requestdto.AiReportRequest(expenseReport);

            AiResponse aiAnalysis = toolDecisionClient.getExpenseSummary(reportRequest);

            if (aiAnalysis == null || aiAnalysis.getMessage() == null || aiAnalysis.getMessage().isEmpty()) {
                log.warn("FastAPI returned empty analysis, using raw report");
                return new AiResponse(expenseReport);
            }

            log.info("FastAPI analysis received successfully");
            return aiAnalysis;

        } catch (Exception e) {
            log.error("Failed to call FastAPI for analysis: {}", e.getMessage(), e);
            // Fallback: return raw report if FastAPI fails
            log.warn("Returning raw expense report due to FastAPI error");
            return new AiResponse(expenseReport);
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
            log.warn("Some expenses not found for deletion: {}", e.getMessage());
            return new AiResponse("Some expenses were not found. They may have already been deleted.");
        } catch (Exception e) {
            log.error("Error handling delete_expense", e);
            throw new ExpenseProcessingException("Failed to delete expense(s): " + e.getMessage(), e);
        }

    }


}
