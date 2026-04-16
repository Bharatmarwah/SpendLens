package com.bharat.SpendLens.exception;

public class MissingFieldException extends RuntimeException {

    public MissingFieldException(String message) {
        super(message);
    }

    public MissingFieldException(String message, Throwable cause) {
        super(message, cause);
    }
}

