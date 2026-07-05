package com.lopatin.reminder.bot;

import com.lopatin.reminder.api.dto.ParsedReminderDto;
import com.lopatin.reminder.api.dto.UpdateDto;
import com.lopatin.reminder.api.response.ReminderResponse;
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
import org.telegram.telegrambots.meta.api.objects.Update;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.time.ZoneOffset.UTC;

@Component
@Slf4j
public class BotUpdateHandler {

    private final UserSettingsService userSettingsService;
    private final UserSettingsRepository userSettingsRepository;
    private final BotReminderService botReminderService;
    private final GroqService groqService;
    private final MessageSender sender;

    private final Map<Long, BotState> states = new HashMap<>();
    private final Map<Long, BotSession> drafts = new HashMap<>();


    public BotUpdateHandler(
                            UserSettingsService userSettingsService,
                            UserSettingsRepository userSettingsRepository,
                            BotReminderService botReminderService,
                            GroqService groqService,
                            @Lazy MessageSender sender) {
        this.userSettingsService = userSettingsService;
        this.userSettingsRepository = userSettingsRepository;
        this.botReminderService = botReminderService;
        this.groqService = groqService;
        this.sender = sender;
    }



    public void handle(Update update){
        if (update.hasCallbackQuery()){
            log.info("New incoming callback from chatId={}", update.getCallbackQuery().getMessage().getChatId());
            handleCallback(update);
            return;
        }
        if (update.hasMessage()){
            log.info("New incoming message from chatId={}", update.getMessage().getChatId());
            handleMessage(update);
        }
    }


    private void handleCallback(Update update) {

        String data = update.getCallbackQuery().getData();
        Long callbackChatId = update.getCallbackQuery().getMessage().getChatId();

        if(data.startsWith("tz:")){
            String userZone = data.substring(3);
            botReminderService.saveTimeZone(callbackChatId, userZone);
            sender.sendMessage(callbackChatId, "Готово", BotKeyboards.commands());
            return;
        }
        if (data.startsWith("ReminderProgress.")) {
            String changeProgress = data.substring(17);
            ReminderProgress progress = null;
            try {
                 progress = ReminderProgress.valueOf(changeProgress);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(callbackChatId, "Неизвестный статус.");
                return;
            }

            BotSession draft = drafts.get(callbackChatId);
            if (draft == null || draft.reminderId == null) {
                sender.sendMessage(callbackChatId, "Сессия устарела, начни заново командой /progress");
                return;
            }
            try {
                botReminderService.changeReminderProgress(
                        callbackChatId,
                        draft.reminderId,
                        progress);
                sender.sendMessage(callbackChatId, "Прогресс напоминания успешно изменен");
                drafts.remove(callbackChatId);
                states.remove(callbackChatId);
            } catch (UserSettingsNotFoundException e) {
                drafts.remove(callbackChatId);
                states.remove(callbackChatId);
                sender.sendMessage(callbackChatId, "Привяжи Telegram заново через API");
            } catch (ReminderNotFoundException e) {
                drafts.remove(callbackChatId);
                sender.sendMessage(callbackChatId, "Напоминание c таким ID не найдено, попробуй другой ID");
                states.put(callbackChatId, BotState.WAITING_CHANGE_PROGRESS);
                drafts.put(callbackChatId, BotSession.builder().build());
            }
        }
    }

    private void handleMessage(Update update) {

        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        if (text == null) {return;}

        String [] parts = text.split(" ");
        BotCommand command = BotCommand.parseCommand(parts[0]);

        if(command == BotCommand.START){

            if (parts.length < 2){
                sender.sendMessage(chatId, "Ссылка недействительна");
                return;
            }

            String linkToken = parts[1];

            try {
                userSettingsService.linkTelegram(linkToken, chatId.toString());
                sender.sendMessage(chatId,
                        "Telegram подключен. Выбери временную зону:",
                        BotKeyboards.timeZones());
            } catch (InvalidLinkTokenException e) {
                log.warn("Invalid link token from chatId={}", chatId, e);
                sender.sendMessage(chatId, "Ссылка недействительна или устарела. Получи новую ссылку");
            }
            return;
        }

        if (command != null){
              handleCommand(command, chatId);
              return;
        }

        handleState(chatId, text);
    }

    private void handleCommand(BotCommand command, Long chatId) {
        if (command == BotCommand.CREATE){
            states.put(chatId, BotState.WAITING_TITLE);
            drafts.put(chatId, BotSession.builder().build());
            sender.sendMessage(chatId, "Введи название напоминания");
            return;
        }

        if (command == BotCommand.LIST){
            List<ReminderResponse> reminderResponseList = null;
            try {
                UserSettings us = userSettingsRepository.findByTelegramChatId(chatId.toString())
                        .orElseThrow();
                ZoneId userZone = ZoneId.of(us.getTimezone());

                reminderResponseList = botReminderService.getList(chatId).stream()
                        .map(rr -> {
                            return new ReminderResponse(rr.id(), rr.title(), rr.description(),
                                    rr.remind().atZone(UTC).withZoneSameInstant(userZone).toLocalDateTime(),
                                    rr.user_id(),
                                    rr.reminderProgress());
                        }).toList();

            } catch (UserSettingsNotFoundException e) {
                states.remove(chatId);
                sender.sendMessage(chatId, "Привяжи Telegram заново через API");
                return;
            }
            if(reminderResponseList.isEmpty()){
                sender.sendMessage(chatId, "У тебя нет напоминаний");
            } else {
                sender.sendMessage(chatId, formatList(reminderResponseList));
            }
            return;
        }

        if(command == BotCommand.DELETE){
            sender.sendMessage(chatId, "Введи id напоминания");
            states.put(chatId, BotState.WAITING_DELETE_ID);
            return;
        }

        if(command == BotCommand.EDIT){
            sender.sendMessage(chatId, "Введи id напоминания");
            states.put(chatId, BotState.WAITING_EDIT_ID);
            return;
        }

        if(command == BotCommand.AI){
            states.put(chatId, BotState.WAITING_AI);
            sender.sendMessage(chatId, "Введи напоминание в свободной форме");
            return;
        }
        if(command == BotCommand.PROGRESS){
            states.put(chatId, BotState.WAITING_CHANGE_PROGRESS);
            drafts.put(chatId, BotSession.builder().build());
            sender.sendMessage( chatId,"Введи id напоминания");
            return;
        }

        if (command == BotCommand.BACK) {
            states.remove(chatId);
            drafts.remove(chatId);
            sender.sendMessage(chatId, "Отменено");
        }
    }

    private void handleState(Long chatId, String message){

        BotState state = states.get(chatId);

        if(state == null){
            sender.sendMessage(chatId, "Что-то пошло не так, начни заново", BotKeyboards.commands());
            return;
        }

        switch (state){
            case WAITING_AI               -> handleWaitingAI(chatId, message);
            
            case WAITING_CHANGE_PROGRESS  -> handleWaitingChangeProgress(chatId, message);
            
            case WAITING_EDIT_ID          -> handleWaitingEditID(chatId, message);
            case WAITING_EDIT_TITLE       -> handleWaitingEditTitle(chatId,message);
            case WAITING_EDIT_DESCRIPTION -> handleWaitingEditDescription(chatId, message);
            case WAITING_EDIT_DATE        -> handleWaitingEditDate(chatId, message); 
            
            case WAITING_DELETE_ID        -> handleWaitingDeleteID(chatId, message);

            case WAITING_TITLE            -> handleWaitingTitle(chatId, message);
            case WAITING_DESCRIPTION      -> handleWaitingDescription(chatId, message);
            case WAITING_DATE             -> handleWaitingDate(chatId, message);
        }

    }





    private void handleWaitingAI(Long chatId, String message) {

        try {
            UserSettings userSettings = userSettingsRepository.findByTelegramChatId(chatId.toString())
                    .orElseThrow( () -> new UserSettingsNotFoundException(chatId.toString()));
            String timezone = userSettings.getTimezone();

            ParsedReminderDto parsedReminderDto = groqService
                    .parse(message, timezone, userSettings.getUserId());


            ReminderResponse savedReminder = botReminderService.create(chatId,
                    parsedReminderDto.title(),
                    parsedReminderDto.description(),
                    parsedReminderDto.remind()
                            .atZone(ZoneId.of(timezone)).withZoneSameInstant(UTC).toOffsetDateTime());

            sender.sendMessage(chatId, "Напоминание успешно создано"
                    + formatReminder(savedReminder, timezone));

            states.remove(chatId);

        } catch (UserSettingsNotFoundException e) {
            states.remove(chatId);
            sender.sendMessage(chatId, "Привяжи Telegram заново через API");
        } catch (GroqServiceException e){
            sender.sendMessage(chatId, "Ошибка парсинга. Переформулируй и попробуй еще раз");
        }
    }
    private void handleWaitingChangeProgress(Long chatId, String message) {
        drafts.get(chatId).reminderId = Long.parseLong(message);
        sender.sendMessage(chatId,"Какой прогресс установить для задачи?", BotKeyboards.changeStatus());
    }
    
    private void handleWaitingEditID(Long chatId, String message) {
        try {
            Long reminderId = Long.parseLong(message);
            drafts.put(chatId, BotSession.builder().reminderId(reminderId).build());
            states.put(chatId, BotState.WAITING_EDIT_TITLE);
            sender.sendMessage(chatId, "Введи новое название или '-' чтобы оставить предыдущее");
        } catch (NumberFormatException e) {
            sender.sendMessage(chatId, "Неверный id, попробуй ещё раз");
        }
    }
    private void handleWaitingEditTitle(Long chatId, String message) {
        drafts.get(chatId).title = message.equals("-") ? null : message;
        states.put(chatId, BotState.WAITING_EDIT_DESCRIPTION);
        sender.sendMessage(chatId, "Введи новое описание или '-' чтобы оставить предыдущее");
    }
    private void handleWaitingEditDescription(Long chatId, String message) {
        drafts.get(chatId).description = message.equals("-") ? null : message;
        states.put(chatId, BotState.WAITING_EDIT_DATE);
        sender.sendMessage(chatId,
                "Введи новую дату в формате dd.mm.yyyy hh:mm или '-' чтобы оставить предыдущую");
    }
    private void handleWaitingEditDate(Long chatId, String message) {
        try {
            UserSettings userSettings = userSettingsRepository
                    .findByTelegramChatId(chatId.toString())
                    .orElseThrow(() -> new UserSettingsNotFoundException(chatId.toString()));

            String tz = userSettings.getTimezone();
            if (tz == null){
                sender.sendMessage(chatId,
                        "Сначала выбери временную зону",
                        BotKeyboards.timeZones());
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
                            .withOffsetSameInstant(UTC);

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
            sender.sendMessage(chatId, "Напоминание успешно изменено");

        } catch (UserSettingsNotFoundException e) {
            states.remove(chatId);
            sender.sendMessage(chatId, "Привяжи Telegram заново через API");
        } catch (ReminderNotFoundException e) {
            states.remove(chatId);
            sender.sendMessage(chatId, "Напоминание не найдено");
        } catch (DateTimeParseException e) {
            sender.sendMessage(chatId, "Неверный формат даты, попробуй ещё раз");
        }
    }

    private void handleWaitingDeleteID(Long chatId, String message) {
        try {
            Long reminderId = Long.parseLong(message);
            botReminderService.remove(chatId, reminderId);
            states.remove(chatId);
            sender.sendMessage(chatId, "Напоминание удалено");
        } catch (UserSettingsNotFoundException e) {
            states.remove(chatId);
            sender.sendMessage(chatId, "Привяжи Telegram заново через API");
        } catch (NumberFormatException e) {
            sender.sendMessage(chatId, "Неверный id, попробуй ещё раз");
        } catch (ReminderNotFoundException e) {
            states.remove(chatId);
            sender.sendMessage(chatId, "Напоминание не найдено");
        }
    }

    private void handleWaitingTitle(Long chatId, String message) {
        states.put(chatId, BotState.WAITING_DESCRIPTION);
        drafts.get(chatId).title = message;
        sender.sendMessage(chatId, "Введи описание напоминания");
    }
    private void handleWaitingDescription(Long chatId, String message) {
        states.put(chatId, BotState.WAITING_DATE);
        drafts.get(chatId).description = message;
        sender.sendMessage(chatId, "Введи дату в формате dd.mm.yyyy hh:mm");
    }
    private void handleWaitingDate(Long chatId, String message) {
        try {
            UserSettings userSettings = userSettingsRepository
                    .findByTelegramChatId(chatId.toString())
                    .orElseThrow(() -> new UserSettingsNotFoundException(chatId.toString()));

            String tz = userSettings.getTimezone();
            if (tz == null){
                sender.sendMessage(chatId,
                        "Сначала выбери временную зону",
                        BotKeyboards.timeZones());
                return;
            }
            ZoneId zone = ZoneId.of(tz);

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            OffsetDateTime utcDate = LocalDateTime
                    .parse(message, formatter)
                    .atZone(zone)
                    .toOffsetDateTime()
                    .withOffsetSameInstant(UTC);


            BotSession draft = drafts.get(chatId);
            botReminderService.create(chatId, draft.title, draft.description, utcDate);
            sender.sendMessage(chatId, "Напоминание успешно создано");
            drafts.remove(chatId);
            states.remove(chatId);

        } catch (DateTimeParseException e) {
            sender.sendMessage(chatId, "Неверный формат, попробуй еще раз");
        } catch (UserSettingsNotFoundException e){
            states.remove(chatId);
            drafts.remove(chatId);
            sender.sendMessage(chatId, "Привяжи Telegram заново через API");
        }
    }
    



    private String formatList(List<ReminderResponse> reminders) {
        StringBuilder sb = new StringBuilder("Твои напоминания:\n\n");
        for (int i = 0; i < reminders.size(); i++) {
            ReminderResponse r = reminders.get(i);
            sb.append(i + 1).append(". ").append("[id:").append(r.id()).append("] ").append(r.title()).append("\n");
            sb.append(" ").append(r.remind()).append("\n");
            sb.append("[status: ").append(r.reminderProgress()).append("]").append("\n");
        }
        return sb.toString();
    }
    private String formatReminder(ReminderResponse reminderResponse, String timezone) {
        return "\n" +
                "ID: " + reminderResponse.id() +  "\n" +
                "Title: " + reminderResponse.title() + "\n" +
                "Description: " + reminderResponse.description() + "\n" +
                "Remind at: " + reminderResponse.remind().atZone(UTC)
                .withZoneSameInstant(ZoneId.of(timezone)).toLocalDateTime() + "\n";
    }



}
