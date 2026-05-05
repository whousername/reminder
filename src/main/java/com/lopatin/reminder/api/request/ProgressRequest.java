package com.lopatin.reminder.api.request;

import com.lopatin.reminder.model.ReminderProgress;

public record ProgressRequest(
        ReminderProgress reminderProgress
) {}
