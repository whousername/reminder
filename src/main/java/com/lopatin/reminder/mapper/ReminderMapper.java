package com.lopatin.reminder.mapper;

import com.lopatin.reminder.api.request.CreateReminderRequest;
import com.lopatin.reminder.api.response.ReminderResponse;
import com.lopatin.reminder.model.Reminder;
import org.springframework.stereotype.Component;



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
    public Reminder dtoToEntity(CreateReminderRequest request) {
        return new Reminder(
                null,
                request.title(),
                request.description(),
                request.remind(),
                request.user_id()
        );
    }

}
