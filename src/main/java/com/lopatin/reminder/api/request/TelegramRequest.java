package com.lopatin.reminder.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TelegramRequest {
    @JsonProperty("chat_id")
    private String chatId;

    @JsonProperty("text")
    private String text;
}