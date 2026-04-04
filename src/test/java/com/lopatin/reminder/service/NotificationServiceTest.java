package com.lopatin.reminder.service;

import com.lopatin.reminder.exception.TelegramServiceException;
import com.lopatin.reminder.exception.UserSettingsNotFoundException;
import com.lopatin.reminder.model.Reminder;
import com.lopatin.reminder.model.ReminderStatus;
import com.lopatin.reminder.model.UserSettings;
import com.lopatin.reminder.repo.ReminderRepository;
import com.lopatin.reminder.repo.UserSettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private TelegramService telegramService;
    @Mock
    private UserSettingsRepository userRepo;
    @Mock
    private ReminderRepository reminderRepo;
    @Mock
    private MailService mailService;

    @InjectMocks
    private NotificationService notificationService;

    private final UUID keycloakUUID = UUID.randomUUID();
    private final String keycloakId = keycloakUUID.toString();
    private final Reminder reminder = Reminder.builder()
            .title("Test1")
            .description("Test1")
            .remind(LocalDateTime.parse("2030-03-27T13:31:10"))
            .userId(keycloakUUID)
            .status(ReminderStatus.PENDING)
            .build();
    private final UserSettings userSettings =
            new UserSettings(keycloakId, "test-chatId",
                    null, "fakemail@test.com");


    @Test
    void sendReminder_statusSENT() {
        when(userRepo.findById(anyString())).thenReturn(Optional.of(userSettings));
        notificationService.sendReminder(reminder);

        assertThat(reminder.getStatus()).isEqualTo(ReminderStatus.SENT);
        verify(reminderRepo).save(reminder);

    }
    @Test
    void sendReminder_statusPARTITIALLY_SENT_mailThrow() {
        when(userRepo.findById(anyString())).thenReturn(Optional.of(userSettings));

        doThrow(new MailSendException("TestMailException"))
                .when(mailService).sendMail(anyString(), anyString(), anyString());

        notificationService.sendReminder(reminder);

        assertThat(reminder.getStatus()).isEqualTo(ReminderStatus.PARTIALLY_SENT);
        verify(reminderRepo).save(reminder);
    }

    @Test
    void sendReminder_statusPARTITIALLY_SENT_TelegramThrow() {
        when(userRepo.findById(anyString())).thenReturn(Optional.of(userSettings));

        doThrow(new TelegramServiceException("TestTelegramException", null))
                .when(telegramService).sendTelegram(anyString(), anyString());

        notificationService.sendReminder(reminder);

        assertThat(reminder.getStatus()).isEqualTo(ReminderStatus.PARTIALLY_SENT);
        verify(reminderRepo).save(reminder);
    }
    @Test
    void sendReminder_statusFAILED() {
        when(userRepo.findById(anyString())).thenReturn(Optional.of(userSettings));

        doThrow(new MailSendException("TestMailException"))
                .when(mailService).sendMail(anyString(), anyString(), anyString());
        doThrow(new TelegramServiceException("TestTelegramException", null))
                .when(telegramService).sendTelegram(anyString(), anyString());

        notificationService.sendReminder(reminder);

        assertThat(reminder.getStatus()).isEqualTo(ReminderStatus.FAILED);
        verify(reminderRepo).save(reminder);
    }
    @Test
    void sendReminder_throwsUserSettingsNotFoundException() {
        when(userRepo.findById(anyString())).thenReturn(Optional.empty());
        assertThrows(UserSettingsNotFoundException.class, ()->
                notificationService.sendReminder(reminder));
    }
}