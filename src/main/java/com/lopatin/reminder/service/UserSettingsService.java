package com.lopatin.reminder.service;

import com.lopatin.reminder.bot.TelegramProperties;
import com.lopatin.reminder.model.UserSettings;
import com.lopatin.reminder.repo.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Slf4j
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
        log.info("New telegram chatId={} just linked", chatId);

    }


    public String generateTgLink(Jwt jwt) {

        String token = UUID.randomUUID().toString();
        String email = jwt.getClaimAsString("email");
        String keycloakId = jwt.getSubject();

        UserSettings entity = repo
                .findById(keycloakId)
                .orElse(entity = UserSettings.builder()
                        .userId(keycloakId)
                        .build());

        entity.setLinkToken(token);
        entity.setEmail(email);
        repo.save(entity);

        log.info("New Telegram-link just generated for userId={}", entity.getUserId());
        return "https://t.me/" + props.getUsername() + "?start=" + token;

    }
}
