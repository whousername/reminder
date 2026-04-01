package com.lopatin.reminder.exception;

import java.util.UUID;

public class UserSettingsNotFoundException extends RuntimeException{
    public UserSettingsNotFoundException (UUID userId){
        super("UserSettings not found for user: " + userId);
    }
}
