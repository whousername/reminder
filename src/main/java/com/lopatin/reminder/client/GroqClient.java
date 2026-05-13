package com.lopatin.reminder.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lopatin.reminder.api.request.GroqRequest;
import com.lopatin.reminder.config.GroqProperties;
import com.lopatin.reminder.exception.GroqClientException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class GroqClient {

    private final ObjectMapper mapper;
    private final HttpClient httpClient;
    private final GroqProperties properties;


    public GroqClient(ObjectMapper mapper, HttpClient httpClient, GroqProperties properties) {
        this.mapper = mapper;
        this.httpClient = httpClient;
        this.properties = properties;
    }

    public HttpResponse<String> sendGroqRequest(GroqRequest groqRequest) {
        try {
            String body = mapper.writeValueAsString(groqRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getUri()))
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200){
                throw new HttpException("GroqAPI response status is "+ response.statusCode());
            }

            return response;

        } catch (IOException | InterruptedException | HttpException e) {
            throw new GroqClientException("Failed to make a request, an GroqApi exception occurred", e);
        }
    }
}
