package com.lopatin.reminder.bot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

public interface MessageSender {

    void sendMessage(Long chatId, String message);

    void sendMessage(Long chatId, String message, InlineKeyboardMarkup keyboard);

    void sendMessage(Long chatId, String message, ReplyKeyboardMarkup keyboard);

    }
