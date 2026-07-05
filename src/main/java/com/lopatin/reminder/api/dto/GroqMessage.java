package com.lopatin.reminder.api.dto;

public record GroqMessage(
        String role,
        String content
) {
}
