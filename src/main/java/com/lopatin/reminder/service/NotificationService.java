package com.lopatin.reminder.service;

import com.lopatin.reminder.model.Reminder;
import com.lopatin.reminder.model.ReminderStatus;
import com.lopatin.reminder.model.UserSettings;
import com.lopatin.reminder.repo.ReminderRepository;
import com.lopatin.reminder.repo.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final TelegramService telegramService;
    private final UserSettingsRepository userRepo;
    private final ReminderRepository reminderRepo;


    public void sendReminder(Long reminderId) {

        Reminder reminder = reminderRepo.findById(reminderId)
                .orElseThrow(()->new RuntimeException("Reminder not found: " + reminderId));

        UserSettings userSettings = userRepo.findById(reminder.getUser_id().toString())
                .orElseThrow(()-> new RuntimeException(
                        "User settings with userId=" + reminder.getUser_id() + " not found."));

        String chatId = userSettings
                .getTelegramChatId();
        String message = reminder
                .getTitle() + " | " + reminder.getDescription();

        try {
            telegramService.sendTelegram(chatId, message);
            reminder.setStatus(ReminderStatus.SENT);
        } catch (Exception e) {
            reminder.setStatus(ReminderStatus.FAILED);
            throw e;
        } finally {
            reminderRepo.save(reminder);
        }


        //еще email тут будет
    }
}
