package com.lopatin.reminder.service;

import com.lopatin.reminder.exception.TelegramServiceException;
import com.lopatin.reminder.exception.UserSettingsNotFoundException;
import com.lopatin.reminder.model.Reminder;
import com.lopatin.reminder.model.ReminderStatus;
import com.lopatin.reminder.model.UserSettings;
import com.lopatin.reminder.repo.ReminderRepository;
import com.lopatin.reminder.repo.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final TelegramService telegramService;
    private final UserSettingsRepository userRepo;
    private final ReminderRepository reminderRepo;
    private final MailService mailService;


    public void sendReminder(Reminder reminder) {

        log.info("Sending reminder id={}", reminder.getId());

        UserSettings userSettings = userRepo.findById(reminder.getUserId().toString())
                .orElseThrow(()-> new UserSettingsNotFoundException(reminder.getUserId()));

        boolean sentTg = false;
        boolean sentMail = false;

        String chatId = userSettings.getTelegramChatId();
        String mail = userSettings.getEmail();

        try {
            if (chatId == null){
                log.warn("Telegram chatId is null for reminder={}, skipping.", reminder.getId());
            }
            else {
                telegramService.sendTelegram(
                        chatId,
                        "/Напоминание: " + reminder.getTitle() +
                                " /Подробности: " + reminder.getDescription());
                sentTg = true;
            }

        }
        catch (TelegramServiceException e) {
            log.error("Failed to send notification via Telegram for reminderId={} ", reminder.getId(), e);
        }
        try{
            if (mail == null){
                log.warn("User mail is null for reminder={}, skipping.", reminder.getId());
            }
            else {
                mailService.sendMail(
                        mail,
                        "/Напоминание: " + reminder.getTitle()  +
                                " /Подробности: ", reminder.getDescription());
                sentMail = true;
            }
        }
        catch (MailException e){
            log.error("Failed to send notification via Email for reminderId={} ", reminder.getId(), e);
        }

        if(sentTg && sentMail){
            reminder.setStatus(ReminderStatus.SENT);
        } else if (sentMail || sentTg) {
            reminder.setStatus(ReminderStatus.PARTIALLY_SENT);
        } else {
            reminder.setStatus(ReminderStatus.FAILED);
        }

        reminderRepo.save(reminder);

        log.info("Reminder id={} status={}", reminder.getId(), reminder.getStatus());
    }
}
