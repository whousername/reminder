package com.lopatin.reminder.api.response;

import com.lopatin.reminder.model.ReminderProgress;

import java.time.LocalDateTime;
import java.util.UUID;


public record ReminderResponse(
        Long id,
        String title,
        String description,
        LocalDateTime remind,
        UUID user_id,
        ReminderProgress reminderProgress

) {
}
