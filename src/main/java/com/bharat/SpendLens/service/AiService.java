package com.bharat.SpendLens.service;

import com.bharat.SpendLens.client.ToolDecisionClient;
import com.bharat.SpendLens.exception.*;
import com.bharat.SpendLens.repository.ExpenseRepo;
import com.bharat.SpendLens.requestdto.AiRequest;
import com.bharat.SpendLens.requestdto.AiReportRequest;
import com.bharat.SpendLens.responsedto.AiResponse;
import com.bharat.SpendLens.responsedto.ExpenseResponseDTO;
import com.bharat.SpendLens.responsedto.ToolMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import com.bharat.SpendLens.requestdto.ExpenseRequestDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final ToolDecisionClient client;
    private final ExpenseService expenseService;
    private final ExpenseRepo expenseRepo;

    public AiResponse askAi(AiRequest request) {

        try {
            ToolMessageResponse body = client.getToolMessageResponse(request);

            if (body == null || body.getType() == null) {
                log.error("Invalid AI response: body is null or type is missing");
                throw new InvalidAiResponseException("Invalid AI response from tool decision service");
            }

            if ("final_answer".equals(body.getType())) {
                log.info("Returning final answer");
                return new AiResponse(body.getMessage());
            }

            if ("tool_call".equals(body.getType())) {

                String toolName = body.getToolName();
                Map<String, Object> args = body.getArguments();

                if (toolName == null || toolName.trim().isEmpty()) {
                    log.error("Tool name is null or empty in tool_call response");
                    throw new InvalidToolResponseException("Invalid tool response");
                }

                if (args == null) {
                    log.error("Arguments are null for tool: {}", toolName);
                    throw new InvalidToolResponseException("Invalid tool response");
                }

                log.info("Tool call detected: {} with args: {}", toolName, args);

                switch (toolName) {

                    case "add_expense":
                        return handleAddExpense(args);


                    case "update_expense":
                        return handleUpdateExpense(args);


                    case "delete_expense":
                        return handleDeleteExpense(args);


                    case "expense_report":
                        return handleExpenseReport(args);

                    default:
                        log.error("Unknown tool requested: {}", toolName);
                        throw new UnknownToolException("Unknown tool: " + toolName);
                }
            }

            log.error("Unexpected response type: {}", body.getType());
            throw new InvalidAiResponseException("Unexpected AI response type: " + body.getType());

        } catch (InvalidAiResponseException | InvalidToolResponseException | UnknownToolException e) {
            log.error("AI request validation error", e);
            throw e;
        } catch (Exception e) {
            log.error("Error processing AI request", e);
            throw new ExpenseProcessingException("Failed to process AI request: " + e.getMessage(), e);
        }
    }

    private AiResponse handleExpenseReport(Map<String, Object> args) {

        try {

            Long expenseId = args.get("id") != null
                    ? Long.parseLong(args.get("id").toString())
                    : null;

            // Extract parameters with null-safe handling
            String category = args.get("category") != null
                    ? args.get("category").toString().toUpperCase().trim()
                    : null;

            BigDecimal minAmount = args.get("min_amount") != null
                    ? new BigDecimal(args.get("min_amount").toString())
                    : null;

            BigDecimal maxAmount = args.get("max_amount") != null
                    ? new BigDecimal(args.get("max_amount").toString())
                    : null;

            // Extract date parameters if provided
            Instant startDate = args.get("start_date") != null
                    ? Instant.parse(args.get("start_date").toString())
                    : null;

            Instant endDate = args.get("end_date") != null
                    ? Instant.parse(args.get("end_date").toString())
                    : null;

            log.info("Generating expense report: category={}, minAmount={}, maxAmount={}, startDate={}, endDate={}",
                    category, minAmount, maxAmount, startDate, endDate);

            // Call service to get filtered expenses
            // Null values are handled by the query (IS NULL checks)
            List<ExpenseResponseDTO> expenses = expenseService.getAllExpenseForUser(
                    expenseId,
                    category,
                    minAmount,
                    maxAmount,
                    startDate,
                    endDate
            );

            if (expenses == null || expenses.isEmpty()) {
                log.info("No expenses found for the given filters");
                return new AiResponse("No expenses found matching your criteria.");
            }

            StringBuilder reportBuilder = new StringBuilder();
            reportBuilder.append("Found ").append(expenses.size()).append(" expense(s):\n\n");

            BigDecimal totalAmount = BigDecimal.ZERO;
            int numberOfExpenses = 0;
            for (ExpenseResponseDTO expense : expenses) {
                reportBuilder.append("• ID: ").append(expense.getId())
                        .append(" | Amount: ₹").append(expense.getAmount())
                        .append(" | Category: ").append(expense.getCategory())
                        .append(" | Description: ").append(expense.getDescription())
                        .append(" | CreatedDate: ").append(expense.getCreatedAt())
                        .append(" | UpdatedDate: ").append(expense.getUpdatedAt())
                        .append("\n");
                totalAmount = totalAmount.add(expense.getAmount());
                numberOfExpenses = expenses.size();
            }

            reportBuilder.append("\nTotal Amount: ₹").append(totalAmount);
            reportBuilder.append("\nNumber of Expenses: ").append(numberOfExpenses);

            String generatedReport = reportBuilder.toString();
            System.out.println("Generated Report:\n" + generatedReport);

            log.info("Expense report generated successfully");


            AiResponse llmSummary = client.getExpenseSummary(new AiReportRequest(generatedReport));

            if (llmSummary != null && llmSummary.getMessage() != null) {
                log.info("LLM summarization completed");
                return new AiResponse(llmSummary.getMessage());
            }

            log.warn("LLM summarization failed, returning generated report");
            return new AiResponse(generatedReport);

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
            Long id = args.get("id") != null
                    ? Long.parseLong(args.get("id").toString())
                    : null;
            if (id == null) {
                throw new MissingFieldException("Missing required field: id");
            }

            try {
                expenseService.deleteExpense(id);
                return new AiResponse("Expense ID " + id + " deleted successfully");
            } catch (ResourceNotFoundException e) {
                // Return a friendly message instead of throwing exception
                log.warn("Expense not found for deletion: id={}", id);
                return new AiResponse("Expense ID " + id + " not found. It may have already been deleted.");
            }
        } catch (MissingFieldException e) {
            log.error("Delete expense validation error", e);
            throw e;
        } catch (Exception e) {
            log.error("Error handling delete_expense", e);
            throw new ExpenseProcessingException("Failed to delete expense: " + e.getMessage(), e);
        }

    }


}