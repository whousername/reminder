package com.lopatin.reminder.api.controller;

import com.lopatin.reminder.api.request.CreateReminderRequest;
import com.lopatin.reminder.api.response.ReminderResponse;
import com.lopatin.reminder.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReminderController {

    private final ReminderService reminderService;

    @PostMapping("/reminder/create")
    public ReminderResponse createReminder(
            @RequestBody
            CreateReminderRequest req)
    {
        return reminderService.create(req);
    }

}