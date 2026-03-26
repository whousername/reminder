package com.lopatin.reminder.api.request;
import java.time.OffsetDateTime;

public record CreateReminderRequest (
        String title,
        String description,
        OffsetDateTime remind
)
{
}
