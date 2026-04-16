package com.bharat.SpendLens.exception;

public class UnknownToolException extends RuntimeException {

    public UnknownToolException(String message) {
        super(message);
    }

    public UnknownToolException(String message, Throwable cause) {
        super(message, cause);
    }
}

