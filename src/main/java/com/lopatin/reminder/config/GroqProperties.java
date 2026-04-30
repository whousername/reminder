package com.lopatin.reminder.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "groq")
@Data
public class GroqProperties {
    private String apiKey;
    private String model;
}
