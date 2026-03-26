package com.lopatin.reminder.api.controller;

import com.lopatin.reminder.api.request.CreateReminderRequest;
import com.lopatin.reminder.api.response.ReminderResponse;
import com.lopatin.reminder.service.UserSettingsService;
import com.lopatin.reminder.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReminderController {

    private final ReminderService reminderService;
    private final UserSettingsService userSettingsService;


    @GetMapping("/api/settings/telegram-link")
    public String telegramLink(@AuthenticationPrincipal Jwt jwt){
        return userSettingsService.generateTgLink(jwt);
    }

    @PostMapping("/reminder/create")
    public ReminderResponse createReminder(
            @RequestBody
            CreateReminderRequest req)
    {
        return reminderService.create(req);
    }

}