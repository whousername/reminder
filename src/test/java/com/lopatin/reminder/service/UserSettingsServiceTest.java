package com.lopatin.reminder.service;

import com.lopatin.reminder.config.TelegramProperties;
import com.lopatin.reminder.exception.InvalidLinkTokenException;
import com.lopatin.reminder.model.UserSettings;
import com.lopatin.reminder.repo.UserSettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSettingsServiceTest {

    @Mock
    private UserSettingsRepository userSettingsRepository;

    @Mock
    private TelegramProperties telegramProperties;

    @InjectMocks
    private UserSettingsService userSettingsService;

    private final String keycloakId = UUID.randomUUID().toString();
    private final String linkToken = UUID.randomUUID().toString();
    private final String chatId = "test-chatId";

    @Test
    public void linkTelegram_shouldSetTelegramChatIdToUser() {
        UserSettings entity = UserSettings.builder()
                        .userId(keycloakId)
                        .telegramChatId(null)
                        .linkToken(linkToken)
                        .email("fakemail@test.com")
                        .build();

        when(userSettingsRepository.findByLinkToken(anyString())).thenReturn(Optional.of(entity));

        userSettingsService.linkTelegram(linkToken, chatId);

        verify(userSettingsRepository).save(any(UserSettings.class));
        assertThat(entity.getTelegramChatId()).isEqualTo(chatId);
        assertThat(entity.getLinkToken()).isNull();
    }

    @Test
    public void linkTelegram_shouldThrowInvalidLinkTokenException() {
        when(userSettingsRepository.findByLinkToken(anyString())).thenReturn(Optional.empty());
        assertThrows(InvalidLinkTokenException.class, () ->
                userSettingsService.linkTelegram(linkToken, chatId));
    }

    @Test
    void generateTgLink_shouldBuildAndSaveEntityWithSetMailAndSetTokenThenReturnLink() {
        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaimAsString(anyString())).thenReturn("fakemail@test.com");
        when(jwt.getSubject()).thenReturn(keycloakId);
        when(userSettingsRepository.findById(any(String.class)))
                .thenReturn(Optional.empty());
        when(telegramProperties.getUsername()).thenReturn("testBot");

        String result = userSettingsService.generateTgLink(jwt);

        verify(userSettingsRepository).findById(anyString());
        verify(userSettingsRepository).save(any(UserSettings.class));

        assertThat(result).startsWith("https://t.me/");
        assertThat(result).contains("?start=");
    }
}