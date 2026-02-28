package com.lopatin.reminder.api.request;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateReminderRequest (
        String title,
        String description,
        LocalDateTime remind
)
{
}
