package com.lopatin.reminder.exception;

public class ReminderSchedulingException extends RuntimeException {
    public ReminderSchedulingException(Long id, Throwable cause){
        super("Failed to schedule reminder: " + id, cause);
    }
}
