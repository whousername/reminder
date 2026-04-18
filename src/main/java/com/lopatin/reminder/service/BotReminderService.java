package com.lopatin.reminder.service;

import com.lopatin.reminder.api.request.CreateReminderRequest;
import com.lopatin.reminder.api.response.ReminderResponse;
import com.lopatin.reminder.exception.UserSettingsNotFoundException;
import com.lopatin.reminder.model.UserSettings;
import com.lopatin.reminder.repo.UserSettingsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class BotReminderService {

    private final UserSettingsRepository userSettingsRepository;
    private final ReminderService reminderService;


    public BotReminderService(UserSettingsRepository userSettingsRepository,
                              ReminderService reminderService) {
        this.userSettingsRepository = userSettingsRepository;
        this.reminderService = reminderService;
    }

    public void create(Long chatId,
                                   String title,
                                   String description,
                                   OffsetDateTime date) {

        UserSettings userSettings = userSettingsRepository.findByTelegramChatId(chatId.toString())
                .orElseThrow(()-> new UserSettingsNotFoundException(chatId.toString()));
        UUID userId = UUID.fromString(userSettings.getUserId());

        log.info("Creating reminder from bot for userId={}",
                userId);

        CreateReminderRequest request = CreateReminderRequest.builder()
                .title(title)
                .description(description)
                .remind(date)
                .build();

        reminderService.create(request, userId);
    }


    public List<ReminderResponse> getList(Long chatId) {
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        //нужен приватный метод?
        UserSettings userSettings = userSettingsRepository.findByTelegramChatId(chatId.toString())
                .orElseThrow(()-> new UserSettingsNotFoundException(chatId.toString()));
        UUID userId = UUID.fromString(userSettings.getUserId());

        return reminderService.getAllReminders(userId, pageable);
    }
}
