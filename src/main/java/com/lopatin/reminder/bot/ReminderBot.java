package com.lopatin.reminder.bot;

import com.lopatin.reminder.api.dto.ParsedReminderDto;
import com.lopatin.reminder.api.dto.UpdateDto;
import com.lopatin.reminder.api.response.ReminderResponse;
import com.lopatin.reminder.config.TelegramProperties;
import com.lopatin.reminder.exception.*;
import com.lopatin.reminder.model.ReminderProgress;
import com.lopatin.reminder.model.UserSettings;
import com.lopatin.reminder.repo.UserSettingsRepository;
import com.lopatin.reminder.service.BotReminderService;
import com.lopatin.reminder.service.GroqService;
import com.lopatin.reminder.service.UserSettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.ZoneOffset.UTC;

@Slf4j
@Component
public class ReminderBot extends TelegramLongPollingBot  implements MessageSender {


    private final TelegramProperties props;
    private final BotUpdateHandler handler;


    public ReminderBot(TelegramProperties props,
                       BotUpdateHandler handler){
        super(props.getToken());
        this.props = props;
        this.handler = handler;
    }


    @Override
    public void onUpdateReceived(Update update) {

        if (update.getMessage() == null && update.getCallbackQuery() == null){return;}
        handler.handle(update);
    }

    @Override
    public void sendMessage(Long chatId, String message){
        SendMessage msg = SendMessage.builder()
                .chatId(chatId.toString())
                .text(message)
                .build();
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            throw new TelegramServiceException("Failed to send message: ",e);
        }
    }
    @Override
    public void sendMessage(Long chatId, String message, InlineKeyboardMarkup keyboard) {
        SendMessage msg = SendMessage.builder()
                .chatId(chatId.toString())
                .text(message)
                .replyMarkup(keyboard)
                .build();
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            throw new TelegramServiceException("Failed to send message: ", e);
        }
    }
    @Override
    public void sendMessage(Long chatId, String message, ReplyKeyboardMarkup keyboard) {
        SendMessage msg = SendMessage.builder()
                .chatId(chatId.toString())
                .text(message)
                .replyMarkup(keyboard)
                .build();
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            throw new TelegramServiceException("Failed to send message: ", e);
        }
    }


    @Override
    public String getBotUsername() {
        return props.getUsername();
    }

}

