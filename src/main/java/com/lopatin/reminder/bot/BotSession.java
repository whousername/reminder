package com.lopatin.reminder.bot;

import lombok.Builder;

@Builder
public class BotSession {
    public String title;
    public String description;
    public Long reminderId;
}

