package com.lopatin.reminder.bot;

import com.lopatin.reminder.model.ReminderProgress;
import lombok.Builder;

@Builder
public class BotSession {
    public String title;
    public String description;
    public Long reminderId;
    public ReminderProgress reminderProgress;
}

