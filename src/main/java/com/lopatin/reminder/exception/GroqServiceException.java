package com.lopatin.reminder.exception;

public class GroqServiceException extends RuntimeException {
    public GroqServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
