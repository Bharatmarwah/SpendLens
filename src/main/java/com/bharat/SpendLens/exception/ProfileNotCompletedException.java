package com.bharat.SpendLens.exception;

public class ProfileNotCompletedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ProfileNotCompletedException(String message) {
        super(message);
    }

    public ProfileNotCompletedException(String message, Throwable cause) {
        super(message, cause);
    }
}

