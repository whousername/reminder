package com.lopatin.reminder.bot;

import com.lopatin.reminder.api.response.ReminderResponse;
import com.lopatin.reminder.exception.InvalidLinkTokenException;
import com.lopatin.reminder.exception.TelegramServiceException;
import com.lopatin.reminder.service.BotReminderService;
import com.lopatin.reminder.service.UserSettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ReminderBot extends TelegramLongPollingBot {


    private final TelegramProperties props;
    private final UserSettingsService userSettingsService;
    private final BotReminderService botReminderService;

    private final Map<Long, BotState> states = new HashMap<>();
    private final Map<Long, BotSession> drafts = new HashMap<>();


    public ReminderBot(TelegramProperties props, UserSettingsService userSettingsService, BotReminderService botReminderService){
        super(props.getToken());
        this.props = props;
        this.userSettingsService = userSettingsService;
        this.botReminderService = botReminderService;
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

            try {
                userSettingsService.linkTelegram(linkToken, chatId.toString());
                sendMessage(chatId, "Telegram подключен!");
            } catch (InvalidLinkTokenException e) {
                log.warn("Invalid link token from chatId={}", chatId, e);
                sendMessage(chatId, "Ссылка недействительна или устарела. Получи новую ссылку.");
            }
        }
        if (text != null && text.equals("/create")){
            states.put(chatId, BotState.WAITING_TITLE);
            drafts.put(chatId, new BotSession());
            sendMessage(chatId, "Введи название напоминания... ");
            return;
        }

        if (text != null && text.equals("/list")){
            List<ReminderResponse> reminderResponseList
                    = botReminderService.getList(chatId);
            if(reminderResponseList.isEmpty()){
                sendMessage(chatId, "У тебя нет напоминаний.");
            } else {
                sendMessage(chatId, formatList(reminderResponseList));
            }
            return;
        }

        if (text != null){
            String message = update.getMessage().getText();
            handleState(chatId, message);
        }
    }

    public void handleState(Long chatId, String message){

        BotState state = states.get(chatId);

        if (state == BotState.WAITING_TITLE){
            states.put(chatId, BotState.WAITING_DESCRIPTION);
            drafts.get(chatId).title = message;
            sendMessage(chatId, "Введи описание напоминания...");
            return;
        }
        if (state == BotState.WAITING_DESCRIPTION){
            states.put(chatId, BotState.WAITING_DATE);
            drafts.get(chatId).description = message;
            sendMessage(chatId, "Введи дату в формате UTC: " + OffsetDateTime.now());
            return;
        }
        if (state == BotState.WAITING_DATE){
            try {
                OffsetDateTime date = OffsetDateTime.parse(message);
                BotSession draft = drafts.get(chatId);
                botReminderService.create(chatId, draft.title, draft.description, date);
                sendMessage(chatId, "Напоминание успешно создано!");
                drafts.remove(chatId);
                states.remove(chatId);

            } catch (DateTimeParseException e) {
                sendMessage(chatId, "Неверный формат, попробуй еще раз...");
            }
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
            throw new TelegramServiceException("Failed to send message: ",e);
        }
    }

    private String formatList(List<ReminderResponse>
                                      reminders) {
        StringBuilder sb = new StringBuilder("Твои напоминания:\n\n");
        for (int i = 0; i < reminders.size(); i++) {
            ReminderResponse r = reminders.get(i);
            sb.append(i + 1).append(". ").append("[id:").append(r.id()).append("] ").append(r.title()).append("\n");
            sb.append(" ").append(r.remind()).append("\n\n");
        }
        return sb.toString();
    }



    @Override
    public String getBotUsername() {
        return props.getUsername();
    }


}
