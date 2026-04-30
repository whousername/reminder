package com.lopatin.reminder.api.dto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record ParsedReminderDto(
        String title,
        String description,
        LocalDateTime remind
) {
}
