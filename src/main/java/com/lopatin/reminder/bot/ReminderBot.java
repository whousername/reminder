package com.lopatin.reminder.bot;

import com.lopatin.reminder.api.request.UpdateDto;
import com.lopatin.reminder.api.response.ReminderResponse;
import com.lopatin.reminder.exception.InvalidLinkTokenException;
import com.lopatin.reminder.exception.ReminderNotFoundException;
import com.lopatin.reminder.exception.TelegramServiceException;
import com.lopatin.reminder.exception.UserSettingsNotFoundException;
import com.lopatin.reminder.model.UserSettings;
import com.lopatin.reminder.repo.UserSettingsRepository;
import com.lopatin.reminder.service.BotReminderService;
import com.lopatin.reminder.service.UserSettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
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
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ReminderBot extends TelegramLongPollingBot {


    private final TelegramProperties props;
    private final UserSettingsService userSettingsService;
    private final UserSettingsRepository userSettingsRepository;
    private final BotReminderService botReminderService;

    private final Map<Long, BotState> states = new HashMap<>();
    private final Map<Long, BotSession> drafts = new HashMap<>();


    public ReminderBot(TelegramProperties props, UserSettingsService userSettingsService, UserSettingsRepository userSettingsRepository, BotReminderService botReminderService){
        super(props.getToken());
        this.props = props;
        this.userSettingsService = userSettingsService;
        this.userSettingsRepository = userSettingsRepository;
        this.botReminderService = botReminderService;
    }



    @Override
    public void onUpdateReceived(Update update) {

        if(update.hasCallbackQuery()){
            String data = update.getCallbackQuery().getData();
            Long callbackChatId = update.getCallbackQuery().getMessage().getChatId();
            if(data.startsWith("tz:")){
                String userZone = data.substring(3);
                botReminderService.saveTimeZone(callbackChatId, userZone);
                sendMessage(callbackChatId, "Готово", buildKeyboardCommands());
            }
            return;
        }

        if (update.getMessage() == null){return;}

        log.info("New incoming message from chatId={}", update.getMessage().getChatId());

        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        if(text != null && text.startsWith("/start")){
            String [] parts = text.split(" ");
            if (parts.length < 2){
                sendMessage(chatId, "Ссылка недействительна");
                return;
            }

            String linkToken = parts[1];

            try {
                userSettingsService.linkTelegram(linkToken, chatId.toString());
                sendMessage(chatId,
                        "Telegram подключен. Выбери временную зону:",
                        buildKeyboardTimeZones());
            } catch (InvalidLinkTokenException e) {
                log.warn("Invalid link token from chatId={}", chatId, e);
                sendMessage(chatId, "Ссылка недействительна или устарела. Получи новую ссылку");
            }
        }

        if (text != null && text.equals("/create")){
            states.put(chatId, BotState.WAITING_TITLE);
            drafts.put(chatId, BotSession.builder().build());
            sendMessage(chatId, "Введи название напоминания");
            return;
        }

        if (text != null && text.equals("/list")){
            List<ReminderResponse> reminderResponseList = null;
            try {
                reminderResponseList = botReminderService.getList(chatId);
            } catch (UserSettingsNotFoundException e) {
                states.remove(chatId);
                sendMessage(chatId, "Привяжи Telegram заново через API");
                return;
            }
            if(reminderResponseList.isEmpty()){
                sendMessage(chatId, "У тебя нет напоминаний");
            } else {
                sendMessage(chatId, formatList(reminderResponseList));
            }
            return;
        }

        if(text != null && text.equals("/delete")){
            sendMessage(chatId, "Введи id напоминания");
            states.put(chatId, BotState.WAITING_DELETE_ID);
            return;
        }

        if(text != null && text.equals("/edit")){

            sendMessage(chatId, "Введи id напоминания");
            states.put(chatId, BotState.WAITING_EDIT_ID);
            return;
        }

        if (text != null && text.equals("/back")) {
            states.remove(chatId);
            drafts.remove(chatId);
            sendMessage(chatId, "Отменено");
            return;
        }

        if (text != null){
            String message = update.getMessage().getText();
            handleState(chatId, message);
        }
    }


    public void handleState(Long chatId, String message){

        BotState state = states.get(chatId);

        if(state == BotState.WAITING_EDIT_ID){
            try {
                Long reminderId = Long.parseLong(message);
                drafts.put(chatId, BotSession.builder().reminderId(reminderId).build());
                states.put(chatId, BotState.WAITING_EDIT_TITLE);
                sendMessage(chatId, "Введи новое название или '-' чтобы оставить предыдущее");
                return;
            } catch (NumberFormatException e) {
                sendMessage(chatId, "Неверный id, попробуй ещё раз");
            }
        }

        if(state == BotState.WAITING_EDIT_TITLE) {
            String title = message.equals("-") ? null : message;
            drafts.put(chatId, BotSession.builder().title(title).build());
            states.put(chatId, BotState.WAITING_EDIT_DESCRIPTION);
            sendMessage(chatId, "Введи новое описание или '-' чтобы оставить предыдущее");
            return;
        }

        if(state == BotState.WAITING_EDIT_DESCRIPTION) {
            String description = message.equals("-") ? null : message;
            drafts.put(chatId, BotSession.builder().description(description).build());
            states.put(chatId, BotState.WAITING_EDIT_DATE);
            sendMessage(chatId,
                    "Введи новую дату в формате dd.mm.yyyy hh:mm или '-' чтобы оставить предыдущую");
            return;
        }
        if (state == BotState.WAITING_EDIT_DATE){

            try {
                UserSettings userSettings = userSettingsRepository
                        .findByTelegramChatId(chatId.toString())
                        .orElseThrow(() -> new UserSettingsNotFoundException(chatId.toString()));

                String tz = userSettings.getTimezone();
                if (tz == null){
                    sendMessage(chatId,
                            "Сначала выбери временную зону",
                            buildKeyboardTimeZones());
                    return;
                }
                ZoneId zone = ZoneId.of(tz);

                DateTimeFormatter formatter =
                        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

                OffsetDateTime utcDate =
                        message.equals("-") ? null : LocalDateTime
                                .parse(message, formatter)
                                .atZone(zone)
                                .toOffsetDateTime()
                                .withOffsetSameInstant(ZoneOffset.UTC);

                UpdateDto updateDto = UpdateDto.builder()
                        .title(drafts.get(chatId).title)
                        .description(drafts.get(chatId).description)
                        .remind(utcDate)
                        .build();

                botReminderService.edit(
                        chatId,
                        drafts.get(chatId).reminderId,
                        updateDto);

                drafts.remove(chatId);
                states.remove(chatId);
                sendMessage(chatId, "Напоминание успешно изменено");
                return;

            } catch (UserSettingsNotFoundException e) {
                states.remove(chatId);
                sendMessage(chatId, "Привяжи Telegram заново через API");
            } catch (ReminderNotFoundException e) {
                states.remove(chatId);
                sendMessage(chatId, "Напоминание не найдено");
            } catch (DateTimeParseException e) {
                sendMessage(chatId, "Неверный формат даты, попробуй ещё раз");
            }
        }


        if(state == BotState.WAITING_DELETE_ID){
            try {
                Long reminderId = Long.parseLong(message);
                botReminderService.remove(chatId, reminderId);
                states.remove(chatId);
                sendMessage(chatId, "Напоминание удалено");
            } catch (UserSettingsNotFoundException e) {
                states.remove(chatId);
                sendMessage(chatId, "Привяжи Telegram заново через API");
                return;
            } catch (NumberFormatException e) {
                sendMessage(chatId, "Неверный id, попробуй ещё раз");
            } catch (ReminderNotFoundException e) {
                states.remove(chatId);
                sendMessage(chatId, "Напоминание не найдено");
            }
        }

        if (state == BotState.WAITING_TITLE){
            states.put(chatId, BotState.WAITING_DESCRIPTION);
            drafts.get(chatId).title = message;
            sendMessage(chatId, "Введи описание напоминания");
            return;
        }
        if (state == BotState.WAITING_DESCRIPTION){
            states.put(chatId, BotState.WAITING_DATE);
            drafts.get(chatId).description = message;
            sendMessage(chatId, "Введи дату в формате dd.mm.yyyy hh:mm");
            return;
        }
        if (state == BotState.WAITING_DATE){
            try {
                UserSettings userSettings = userSettingsRepository
                        .findByTelegramChatId(chatId.toString())
                        .orElseThrow(() -> new UserSettingsNotFoundException(chatId.toString()));

                String tz = userSettings.getTimezone();
                if (tz == null){
                    sendMessage(chatId,
                            "Сначала выбери временную зону",
                            buildKeyboardTimeZones());
                    return;
                }
                ZoneId zone = ZoneId.of(tz);

                DateTimeFormatter formatter =
                        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

                OffsetDateTime utcDate = LocalDateTime
                                .parse(message, formatter)
                                .atZone(zone)
                                .toOffsetDateTime()
                                .withOffsetSameInstant(ZoneOffset.UTC);


                BotSession draft = drafts.get(chatId);
                botReminderService.create(chatId, draft.title, draft.description, utcDate);
                sendMessage(chatId, "Напоминание успешно создано");
                drafts.remove(chatId);
                states.remove(chatId);

            } catch (DateTimeParseException e) {
                sendMessage(chatId, "Неверный формат, попробуй еще раз");
            } catch (UserSettingsNotFoundException e){
                states.remove(chatId);
                drafts.remove(chatId);
                sendMessage(chatId, "Привяжи Telegram заново через API");
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

    private void sendMessage(Long chatId, String message, InlineKeyboardMarkup keyboard)
    {
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

    private void sendMessage(Long chatId, String message, ReplyKeyboardMarkup keyboard)
    {
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



    private InlineKeyboardMarkup buildKeyboardTimeZones() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder().text("Москва UTC+3")
                                .callbackData("tz:Europe/Moscow").build(),
                        InlineKeyboardButton.builder().text("Калининград UTC+2")
                                .callbackData("tz:Europe/Kaliningrad").build()))
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder().text("Самара UTC+4")
                                .callbackData("tz:Europe/Samara").build(),
                        InlineKeyboardButton.builder().text("Екатеринбург UTC+5")
                                .callbackData("tz:Asia/Yekaterinburg").build()))
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder().text("Омск UTC+6")
                                .callbackData("tz:Asia/Omsk").build(),
                        InlineKeyboardButton.builder().text("Красноярск UTC+7")
                                .callbackData("tz:Asia/Krasnoyarsk").build()))
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder().text("Иркутск UTC+8")
                                .callbackData("tz:Asia/Irkutsk").build(),
                        InlineKeyboardButton.builder().text("Якутск UTC+9")
                                .callbackData("tz:Asia/Yakutsk").build()))
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder().text("Владивосток UTC+10")
                                .callbackData("tz:Asia/Vladivostok").build(),
                        InlineKeyboardButton.builder().text("Магадан UTC+11")
                                .callbackData("tz:Asia/Magadan").build()))
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder().text("Анадырь UTC+12")
                                .callbackData("tz:Asia/Anadyr").build()))
                .build();
    }



    private ReplyKeyboardMarkup buildKeyboardCommands(){
        return ReplyKeyboardMarkup.builder()
                .keyboardRow(new
                        KeyboardRow(List.of(
                        new KeyboardButton("/create"),
                        new KeyboardButton("/list"))))
                .keyboardRow(new
                        KeyboardRow(List.of(
                        new KeyboardButton("/delete"),
                        new KeyboardButton("/edit"))))
                .keyboardRow(new
                        KeyboardRow(List.of(
                        new KeyboardButton("/back"))))
                .resizeKeyboard(true)
                .build();
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
