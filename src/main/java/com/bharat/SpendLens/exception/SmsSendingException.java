package com.bharat.SpendLens.exception;

public class SmsSendingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SmsSendingException(String message) {
        super(message);
    }

    public SmsSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}

