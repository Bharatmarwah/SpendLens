package com.bharat.SpendLens.service;

import com.bharat.SpendLens.client.ToolDecisionClient;
import com.bharat.SpendLens.entity.Expense;
import com.bharat.SpendLens.repository.ExpenseRepo;
import com.bharat.SpendLens.requestdto.AiRequest;
import com.bharat.SpendLens.responsedto.AiResponse;
import com.bharat.SpendLens.responsedto.ExpenseResponseDTO;
import com.bharat.SpendLens.responsedto.ToolMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import com.bharat.SpendLens.requestdto.ExpenseRequestDTO;

import java.math.BigDecimal;
import java.time.Instant;
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
                throw new RuntimeException("Invalid AI response from tool decision service");
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
                    throw new RuntimeException("Invalid tool response: tool_name is missing");
                }

                if (args == null) {
                    log.error("Arguments are null for tool: {}", toolName);
                    throw new RuntimeException("Invalid tool response: arguments are missing");
                }

                log.info("Tool call detected: {} with args: {}", toolName, args);

                switch (toolName) {

                    case "add_expense":
                        return handleAddExpense(args);

                    case "update_expense":
                        return handleUpdateExpense(args);


                    case "get_expense_report":
                        log.info("get_expense_report tool not yet implemented");
                        return new AiResponse("Expense report feature coming soon!");

                    default:
                        log.error("Unknown tool requested: {}", toolName);
                        throw new RuntimeException("Unknown tool: " + toolName);
                }
            }

            log.error("Unexpected response type: {}", body.getType());
            throw new RuntimeException("Unexpected AI response type: " + body.getType());

        } catch (Exception e) {
            log.error("Error processing AI request", e);
            throw new RuntimeException("Failed to process AI request: " + e.getMessage(), e);
        }
    }

    private AiResponse handleAddExpense(Map<String, Object> args) {

        try {
            Object amountObj = args.get("amount");
            Object categoryObj = args.get("category");

            if (amountObj == null) {
                log.error("Amount is missing from add_expense arguments");
                throw new RuntimeException("Missing required field: amount");
            }

            if (categoryObj == null) {
                log.error("Category is missing from add_expense arguments");
                throw new RuntimeException("Missing required field: category");
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(amountObj.toString());

                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    log.error("Invalid amount: amount must be > 0, got {}", amount);
                    throw new RuntimeException("Amount must be greater than 0");
                }

            } catch (NumberFormatException e) {
                log.error("Invalid amount format: {}", amountObj, e);
                throw new RuntimeException("Invalid amount: " + amountObj);
            }

            String category = categoryObj.toString().toUpperCase().trim();
            if (category.isEmpty()) {
                log.error("Category is empty");
                throw new RuntimeException("Category cannot be empty");
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

        } catch (Exception e) {
            log.error("Error handling add_expense", e);
            throw new RuntimeException("Failed to add expense: " + e.getMessage(), e);
        }
    }

    private AiResponse handleUpdateExpense(Map<String, Object> args) {

        try {
            Long id = args.get("id") != null
                    ? Long.parseLong(args.get("id").toString())
                    : null;

            if (id == null) {
                throw new RuntimeException("Missing required field: id");
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

        } catch (Exception e) {
            log.error("Error handling update_expense", e);
            throw new RuntimeException("Failed to update expense: " + e.getMessage(), e);
        }
    }
}