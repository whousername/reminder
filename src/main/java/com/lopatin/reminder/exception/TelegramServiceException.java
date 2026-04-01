package com.lopatin.reminder.exception;

public class TelegramServiceException extends RuntimeException {
    public TelegramServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
