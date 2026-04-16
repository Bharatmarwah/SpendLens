package com.bharat.SpendLens.exception;

public class ExpenseProcessingException extends RuntimeException {

    public ExpenseProcessingException(String message) {
        super(message);
    }

    public ExpenseProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

