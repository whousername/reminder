package com.lopatin.reminder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lopatin.reminder.api.dto.GroqMessage;
import com.lopatin.reminder.api.dto.ParsedReminderDto;
import com.lopatin.reminder.api.request.GroqRequest;
import com.lopatin.reminder.api.response.GroqApiResponse;
import com.lopatin.reminder.config.GroqProperties;
import com.lopatin.reminder.exception.GroqServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpException;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.*;
import java.util.List;

@Slf4j
@Service
public class GroqService {

    private final ObjectMapper mapper;
    private final GroqProperties properties;
    private final HttpClient httpClient;

    public GroqService(ObjectMapper mapper, GroqProperties properties, HttpClient httpClient) {
        this.mapper = mapper;
        this.properties = properties;
        this.httpClient = httpClient;
    }

    public ParsedReminderDto parse(String message, String timezone, String userId){

        log.info("Sending request to GroqAPI from userId={}", userId);

        String promt = "  Ты парсер напоминаний. Пользователь присылает текст. Ты извлекаешь title, description, remind (дату).\n" +
                "  Если напоминание простое и ты посчитал что description тут отсутствует, то вместо него проставь null.\n" +
                "  Текст для парсинга: " + message + "\n" +
                "  Сегодня " + ZonedDateTime.now(ZoneId.of(timezone)) + ", timezone " + timezone + ".\n" +
                "  В ответ верни ТОЛЬКО JSON, без пояснений и markdown. \n" +
                "  {\n" +
                "  \"title\": \"\",\n" +
                "  \"description\": \"\",\n" +
                "  \"remind\": \"yyyy-MM-ddTHH:mm\"\n" +
                "  }";

        GroqRequest groqRequest = new GroqRequest(
                properties.getModel(),
                List.of(new GroqMessage("user", promt)));

        try {
            String body = mapper.writeValueAsString(groqRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200){
                throw new HttpException("GroqAPI response status is "+ response.statusCode());
            }

            String content = mapper.readValue(response.body(), GroqApiResponse.class)
                    .choices().get(0).message().content();

            return mapper.readValue(content, ParsedReminderDto.class);

        } catch (Exception e) {
            throw new GroqServiceException("Failed to parse, an GroqApi exception occurred",e);
        }
    }

}
