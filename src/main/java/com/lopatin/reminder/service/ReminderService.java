package com.lopatin.reminder.service;

import com.lopatin.reminder.api.request.CreateReminderRequest;
import com.lopatin.reminder.mapper.ReminderMapper;
import com.lopatin.reminder.api.response.ReminderResponse;
import com.lopatin.reminder.mapper.UserProvider;
import com.lopatin.reminder.repo.ReminderRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ReminderService {

    private final ReminderMapper mapper;
    private final  ReminderRepository repo;
    private final UserProvider userProvider;

    public ReminderService(ReminderMapper mapper, ReminderRepository repo, UserProvider userProvider) {
        this.mapper = mapper;
        this.repo = repo;
        this.userProvider = userProvider;
    }




    public ReminderResponse create(CreateReminderRequest request) {

        UUID user_id = userProvider.getUser_id();
        var newEntity = mapper.dtoToEntity(request, user_id);
        var savedEntity = repo.save(newEntity);
        return mapper.entityToResponse(savedEntity);
    }

}
