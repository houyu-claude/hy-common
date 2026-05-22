package com.houyu.common.app.exception;

public class AccessDeniedException extends RuntimeException {

    private final String code;

    public AccessDeniedException(String message) {
        super(message);
        this.code = "ACCESS_DENIED";
    }

    public AccessDeniedException(String code, String message) {
        super(message);
        this.code = code;
    }

    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
        this.code = "ACCESS_DENIED";
    }

    public String getCode() {
        return code;
    }
}