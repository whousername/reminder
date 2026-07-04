package com.lopatin.reminder.bot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;


public class BotKeyboards {

    public static InlineKeyboardMarkup timeZones() {
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

    public static InlineKeyboardMarkup changeStatus() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder().text("IN_PROGRESS")
                                .callbackData("ReminderProgress.IN_PROGRESS").build(),
                        InlineKeyboardButton.builder().text("DONE")
                                .callbackData("ReminderProgress.DONE").build()))
                .build();

    }

    public static ReplyKeyboardMarkup commands(){
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
                        new KeyboardButton("/progress"))))
                .keyboardRow(new
                        KeyboardRow(List.of(
                        new KeyboardButton("/AI"))))
                .keyboardRow(new
                        KeyboardRow(List.of(
                        new KeyboardButton("/back"))))
                .resizeKeyboard(true)
                .build();
    }


}
