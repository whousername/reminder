package com.lopatin.reminder.repo;

import com.lopatin.reminder.model.Reminder;
import com.lopatin.reminder.model.ReminderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder,Long>, JpaSpecificationExecutor<Reminder> {

    int deleteByIdAndUserId(Long id, UUID userId);

    Optional<Reminder> findByIdAndUserId(Long id, UUID userId);

    List<Reminder> findAllByUserId(UUID userId);

    List<Reminder> findAllByUserIdAndStatus(UUID userId, Pageable pageable, ReminderStatus status);

}
