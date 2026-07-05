package com.lopatin.reminder.api.response;

import com.lopatin.reminder.api.dto.GroqMessage;

public record GroqChoice(
        GroqMessage message
) {
}
