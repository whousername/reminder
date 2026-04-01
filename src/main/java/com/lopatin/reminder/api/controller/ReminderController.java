package com.lopatin.reminder.api.controller;

import com.lopatin.reminder.api.request.CreateReminderRequest;
import com.lopatin.reminder.api.request.UpdateDto;
import com.lopatin.reminder.api.response.ReminderPageResponse;
import com.lopatin.reminder.api.response.ReminderResponse;
import com.lopatin.reminder.service.UserSettingsService;
import com.lopatin.reminder.service.ReminderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReminderController {

    private final ReminderService reminderService;
    private final UserSettingsService userSettingsService;


    @GetMapping("/settings/telegram-link")
    public String telegramLink(@AuthenticationPrincipal Jwt jwt){
        return userSettingsService.generateTgLink(jwt);
    }

    @PostMapping("/reminder/create")
    public ReminderResponse createReminder(
            @RequestBody @Valid
            CreateReminderRequest req) {
        return reminderService.create(req);
    }


    @GetMapping("/reminder/list")
    public ReminderPageResponse getAllReminders(
            @RequestParam(required = false) String search,

            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,

            @RequestParam(defaultValue = "remind") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,

            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "10") int size)
    {
        Sort sort = direction.equals("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return reminderService.getAllReminders(search, dateFrom, dateTo, pageable);
    }


    @DeleteMapping("/reminder/remove/{id}")
    public ResponseEntity<Void> removeReminderById(@PathVariable Long id){
        reminderService.removeReminderById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reminder/{id}")
    public ResponseEntity<ReminderResponse> edit
            (@PathVariable Long id,
            @RequestBody @Valid UpdateDto dataToChange){
        return ResponseEntity.ok(reminderService.editReminderById(id, dataToChange));
    }


}