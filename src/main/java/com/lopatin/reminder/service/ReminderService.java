package com.lopatin.reminder.service;

import com.lopatin.reminder.api.request.CreateReminderRequest;
import com.lopatin.reminder.mapper.ReminderMapper;
import com.lopatin.reminder.api.response.ReminderResponse;
import com.lopatin.reminder.repo.ReminderRepository;
import org.springframework.stereotype.Service;

@Service
public class ReminderService {

    private final ReminderMapper mapper;
    private final  ReminderRepository repo;

    public ReminderService(ReminderMapper mapper, ReminderRepository repo) {
        this.mapper = mapper;
        this.repo = repo;
    }




    public ReminderResponse create(CreateReminderRequest request) {

        var newEntity = mapper.dtoToEntity(request);
        var savedEntity = repo.save(newEntity);
        return mapper.entityToResponse(savedEntity);
    }

}
