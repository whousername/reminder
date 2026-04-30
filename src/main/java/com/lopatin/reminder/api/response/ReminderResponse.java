package com.lopatin.reminder.api.response;

import java.time.LocalDateTime;
import java.util.UUID;


public record ReminderResponse(
        Long id,
        String title,
        String description,
        LocalDateTime remind,
        UUID user_id

) {
}
