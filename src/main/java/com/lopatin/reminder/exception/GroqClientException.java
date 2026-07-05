package com.lopatin.reminder.exception;

public class GroqClientException extends RuntimeException {
    public GroqClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
