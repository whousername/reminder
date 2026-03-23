package com.lopatin.reminder.service;

import com.lopatin.reminder.bot.TelegramProperties;
import com.lopatin.reminder.model.UserSettings;
import com.lopatin.reminder.repo.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserSettingsService {

    private final UserSettingsRepository repo;
    private final TelegramProperties props;


    public void linkTelegram(String linkToken, String chatId) {
        UserSettings entity = repo.findByLinkToken(linkToken)
                        .orElseThrow(() -> new RuntimeException("Недействительный токен."));

        entity.setTelegramChatId(chatId);
        entity.setLinkToken(null);
        repo.save(entity);
    }


    public String generateTgLink(String keycloakId) {

        String token = UUID.randomUUID().toString();

        UserSettings entity = repo
                .findById(keycloakId)
                .orElse(entity = UserSettings.builder()
                        .userId(keycloakId)
                        .build());

        entity.setLinkToken(token);
        repo.save(entity);

        return "https://t.me/" + props.getUsername() + "?start=" + token;
    }
}
