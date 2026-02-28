package com.lopatin.reminder.mapper;

import com.lopatin.reminder.api.request.CreateReminderRequest;
import com.lopatin.reminder.api.response.ReminderResponse;
import com.lopatin.reminder.model.Reminder;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
public class ReminderMapper {

    public ReminderResponse entityToResponse(Reminder savedEntity) {
        return new ReminderResponse(
                savedEntity.getId(),
                savedEntity.getTitle(),
                savedEntity.getDescription(),
                savedEntity.getRemind(),
                savedEntity.getUser_id()
        );
    }
    public Reminder dtoToEntity(CreateReminderRequest request, UUID user_id) {
        return new Reminder(
                null,
                request.title(),
                request.description(),
                request.remind(),
                user_id
        );
    }

}
