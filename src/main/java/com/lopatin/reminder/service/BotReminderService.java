package com.lopatin.reminder.service;

import com.lopatin.reminder.api.request.CreateReminderRequest;
import com.lopatin.reminder.api.dto.UpdateDto;
import com.lopatin.reminder.api.response.ReminderResponse;
import com.lopatin.reminder.exception.UserSettingsNotFoundException;
import com.lopatin.reminder.model.ReminderProgress;
import com.lopatin.reminder.model.UserSettings;
import com.lopatin.reminder.repo.UserSettingsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final UserSettingsService userSettingsService;

    @Value("${telegram.bot.list.page}")
    private int defaultPage;
    @Value("${telegram.bot.list.size}")
    private int defaultSize;



    public BotReminderService(UserSettingsRepository userSettingsRepository,
                              ReminderService reminderService, UserSettingsService userSettingsService) {
        this.userSettingsRepository = userSettingsRepository;
        this.reminderService = reminderService;
        this.userSettingsService = userSettingsService;
    }

    public ReminderResponse create
            (Long chatId,
            String title,
            String description,
            OffsetDateTime date) {

        UUID userId = getUserIdFromChatId(chatId);

        log.info("Creating reminder from bot for userId={}",
                userId);

        CreateReminderRequest request = CreateReminderRequest.builder()
                .title(title)
                .description(description)
                .remind(date)
                .build();

        return reminderService.create(request, userId);
    }


    public List<ReminderResponse> getList(Long chatId) {

        Pageable pageable = PageRequest.of(defaultPage, defaultSize);

        UUID userId = getUserIdFromChatId(chatId);
        return reminderService.getAllReminders(userId, pageable);
    }

    public void remove(Long chatId, Long reminderId) {
        UUID userId = getUserIdFromChatId(chatId);
        reminderService.removeReminderById(userId, reminderId);
    }

    public void edit(Long chatId, Long reminderId, UpdateDto updateDto) {
        UUID userId = getUserIdFromChatId(chatId);
        reminderService.editReminderById(userId, reminderId, updateDto);
    }

    public void saveTimeZone(Long chatId, String userZone) {
        UUID userId = getUserIdFromChatId(chatId);
        userSettingsService.linkTimeZone(userId, userZone);
    }

    public void changeReminderProgress(Long chatId, Long reminderId, ReminderProgress reminderProgress) {

        UUID userId = getUserIdFromChatId(chatId);
        reminderService.changeReminderProgress(
                userId,
                reminderId,
                reminderProgress);

    }

    private UUID getUserIdFromChatId(Long chatId){
        UserSettings userSettings = userSettingsRepository.findByTelegramChatId(chatId.toString())
                .orElseThrow(()-> new UserSettingsNotFoundException(chatId.toString()));
        return UUID.fromString(userSettings.getUserId());
    }


}
