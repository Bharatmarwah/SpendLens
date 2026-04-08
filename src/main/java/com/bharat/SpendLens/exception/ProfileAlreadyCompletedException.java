package com.bharat.SpendLens.exception;

public class ProfileAlreadyCompletedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ProfileAlreadyCompletedException(String message) {
        super(message);
    }

    public ProfileAlreadyCompletedException(String message, Throwable cause) {
        super(message, cause);
    }
}

