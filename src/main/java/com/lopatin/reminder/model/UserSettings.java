package com.lopatin.reminder.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data

@Entity
@Table(name = "user_settings_service")
public class UserSettings {

    @Id
    @Column(name = "keycloak_user_id")
    private String userId;

    @Column(name = "telegram_chat_id")
    private String telegramChatId;

    @Column(name = "link_token")
    private String linkToken;

    @Column(name = "email")
    private String email;


}
