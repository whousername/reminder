package com.lopatin.reminder.exception;

public class InvalidLinkTokenException extends RuntimeException {
    public InvalidLinkTokenException(String message) {
        super(message);
    }
}
