package com.lopatin.reminder.api.response;

import java.time.LocalDateTime;


public record ReminderResponse(
        Long id,
        String title,
        String description,
        LocalDateTime remind,
        java.util.UUID user_id

) {
}
