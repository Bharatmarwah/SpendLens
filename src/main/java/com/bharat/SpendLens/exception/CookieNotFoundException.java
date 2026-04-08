package com.bharat.SpendLens.exception;

public class CookieNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CookieNotFoundException(String message) {
        super(message);
    }

    public CookieNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

