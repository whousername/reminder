package com.lopatin.reminder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lopatin.reminder.api.request.TelegramRequest;
import com.lopatin.reminder.bot.TelegramProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TelegramService {

    private final HttpClient httpClient;
    private final TelegramProperties props;
    private final ObjectMapper mapper;

    public void sendTelegram(String chatId, String message) {

        try {

            String body = mapper
                    .writeValueAsString(new TelegramRequest(chatId, message));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.telegram.org/bot" + props.getToken() + "/sendMessage"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Telegram error: " + response.body());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to send Telegram message: ", e);

        }
    }
}
