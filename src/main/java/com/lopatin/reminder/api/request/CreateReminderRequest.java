package com.lopatin.reminder.api.request;

import java.time.LocalDateTime;

public record CreateReminderRequest (
        String title,
        String description,
        LocalDateTime remind,
        int user_id
)
{
}
