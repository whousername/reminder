package com.lopatin.reminder.service;

import com.lopatin.reminder.api.request.CreateReminderRequest;
import com.lopatin.reminder.mapper.ReminderMapper;
import com.lopatin.reminder.api.response.ReminderResponse;
import com.lopatin.reminder.mapper.UserProvider;
import com.lopatin.reminder.repo.ReminderRepository;
import com.lopatin.reminder.scheduler.ReminderSchedulerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Service
public class ReminderService {

    private final ReminderMapper mapper;
    private final  ReminderRepository repo;
    private final UserProvider userProvider;
    private final ReminderSchedulerService schedulerService;

    public ReminderService
            (ReminderMapper mapper,
             ReminderRepository repo,
             UserProvider userProvider,
             ReminderSchedulerService schedulerService)
    {
        this.mapper = mapper;
        this.repo = repo;
        this.userProvider = userProvider;
        this.schedulerService = schedulerService;
    }




    @Transactional
    public ReminderResponse create(CreateReminderRequest request){

        UUID user_id = userProvider.getUser_id();
        var savedReminder = repo.save(mapper.dtoToEntity(request, user_id));

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        schedulerService.scheduleReminder(
                                savedReminder.getId(),
                                savedReminder.getRemind()
                        );
                    }
                });
        return mapper.entityToResponse(savedReminder);
    }

}
