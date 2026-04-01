package com.lopatin.reminder.exception;

import com.lopatin.reminder.api.response.ErrorResponse;
import com.lopatin.reminder.service.TelegramService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(ReminderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ReminderNotFoundException e){
        log.warn(e.getMessage());
        return new ErrorResponse(e.getMessage(), 404);
    }

    @ExceptionHandler(UserSettingsNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserSettingsNotFound(UserSettingsNotFoundException e){
        log.warn(e.getMessage());
        return new ErrorResponse(e.getMessage(), 404);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadJson (MethodArgumentNotValidException e){
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", message);
        return new ErrorResponse(message, 400);
    }

    @ExceptionHandler(ReminderSchedulingException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleScheduling(ReminderSchedulingException e) {
        log.error(e.getMessage(), e);
        return new ErrorResponse("Failed to schedule reminder", 500);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadJson(HttpMessageNotReadableException e) {
        log.warn("Invalid request body: {}", e.getMessage());
        return new ErrorResponse("Invalid request body", 400);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAll (Exception e){
        log.error("Unexpected error", e);
        return new ErrorResponse("Internal server error.", 500);
    }

}
