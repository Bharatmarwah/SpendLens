package com.bharat.SpendLens.exception;

public class InvalidToolResponseException extends RuntimeException {

    public InvalidToolResponseException(String message) {
        super(message);
    }

    public InvalidToolResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}

