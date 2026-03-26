package com.lopatin.reminder.service;

import com.lopatin.reminder.model.Reminder;
import com.lopatin.reminder.model.ReminderStatus;
import com.lopatin.reminder.model.UserSettings;
import com.lopatin.reminder.repo.ReminderRepository;
import com.lopatin.reminder.repo.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final TelegramService telegramService;
    private final UserSettingsRepository userRepo;
    private final ReminderRepository reminderRepo;
    private final MailService mailService;


    public void sendReminder(Long reminderId) {

        log.info("Sending reminder id={}", reminderId);

        Reminder reminder = reminderRepo.findById(reminderId)
                .orElseThrow(()->new RuntimeException("Reminder not found: " + reminderId));

        UserSettings userSettings = userRepo.findById(reminder.getUser_id().toString())
                .orElseThrow(()-> new RuntimeException(
                        "User settings with userId=" + reminder.getUser_id() + " not found."));

        boolean sentTg = false;
        boolean sentMail = false;

        try {
            telegramService.sendTelegram(
                    userSettings.getTelegramChatId(),
                    "Напоминание: " + reminder.getTitle() +
                            " | " + reminder.getDescription());
            sentTg = true;
        }
        catch (Exception e) {
            log.error("Failed to send notification via Telegram for reminderId={} ", reminderId, e);
        }
        try{
            mailService.sendMail(
                    userSettings.getEmail(),
                    "Напоминание: " + reminder.getTitle(),
                    reminder.getDescription());
            sentMail = true;
        }
        catch (Exception e){
            log.error("Failed to send notification via Email for reminderId={} ", reminderId, e);
        }

        if(sentTg && sentMail){
            reminder.setStatus(ReminderStatus.SENT);
        } else if (sentMail || sentTg) {
            reminder.setStatus(ReminderStatus.PARTIALLY_SENT);
        } else {
            reminder.setStatus(ReminderStatus.FAILED);
        }

        reminderRepo.save(reminder);

        log.info("Reminder id={} status={}", reminderId, reminder.getStatus());
    }
}
