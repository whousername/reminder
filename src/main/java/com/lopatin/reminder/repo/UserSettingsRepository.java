package com.lopatin.reminder.repo;

import com.lopatin.reminder.model.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSettingsRepository extends JpaRepository<UserSettings, String> {


    Optional<UserSettings> findByLinkToken(String linkToken);

    Optional<UserSettings> findById(String keycloakId);
}
