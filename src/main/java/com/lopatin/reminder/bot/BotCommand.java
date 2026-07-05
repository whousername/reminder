package com.lopatin.reminder.bot;

import lombok.Getter;

@Getter
public enum BotCommand {
    START ("/start"),
    CREATE ("/create"),
    LIST ("/list"),
    DELETE ("/delete"),
    EDIT ("/edit"),
    AI ("/AI"),
    PROGRESS ("/progress"),
    BACK ("/back");

    private final String command;

    BotCommand(String command) {
        this.command = command;
    }

    static BotCommand parseCommand(String command){
        try {
            return BotCommand.valueOf(command.substring(1).toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
