package com.lopatin.reminder.bot;

import com.lopatin.reminder.service.UserSettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class ReminderBot extends TelegramLongPollingBot {


    private final TelegramProperties props;
    private final UserSettingsService userSettingsService;


    public ReminderBot(TelegramProperties props, UserSettingsService userSettingsService){
        super(props.getToken());
        this.props = props;
        this.userSettingsService = userSettingsService;
    }



    @Override
    public void onUpdateReceived(Update update) {

        if (update.getMessage() == null){return;}

        log.info("New incoming message from chatId={}", update.getMessage().getChatId());

        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        if(text != null && text.startsWith("/start")){
            String [] parts = text.split(" ");
            if (parts.length < 2){
                sendMessage(chatId, "Ссылка недействительна!");
                return;
            }

            String linkToken = parts[1];
            userSettingsService.linkTelegram(linkToken, chatId.toString());
            sendMessage(chatId, "Telegram подключен!");

        }
    }

    private void sendMessage(Long chatId, String message){
        SendMessage msg = SendMessage.builder()
                .chatId(chatId.toString())
                .text(message)
                .build();
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Failed to send message: ",e);
        }
    }


    @Override
    public String getBotUsername() {
        return props.getUsername();
    }


}
