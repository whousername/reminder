package com.lopatin.reminder.service;


import com.lopatin.reminder.api.request.CreateReminderRequest;
import com.lopatin.reminder.api.response.ReminderResponse;
import com.lopatin.reminder.mapper.ReminderMapper;
import com.lopatin.reminder.mapper.UserProvider;
import com.lopatin.reminder.model.Reminder;
import com.lopatin.reminder.model.ReminderStatus;
import com.lopatin.reminder.repo.ReminderRepository;
import com.lopatin.reminder.scheduler.ReminderSchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReminderServiceTest {

    @Mock
    private ReminderRepository repo;

    @Mock
    private ReminderMapper mapper;

    @Mock
    private UserProvider provider;

    @Mock
    private ReminderSchedulerService reminderSchedulerService;

    @InjectMocks
    private ReminderService reminderService;

    @BeforeEach
    void setUp() {
    }

    @Test
    public void reminderService_createReminder_shouldMapSaveAndReturnResponse() {

        OffsetDateTime requestTime = OffsetDateTime.parse(
                "2030-03-27T13:31:10Z");

        LocalDateTime localDateTime = LocalDateTime.parse(
                "2030-03-27T13:31:10");

        UUID user_id = UUID.randomUUID();

        Reminder entityBeforeSave = new Reminder(
                null, "test1", "test1", localDateTime, user_id, ReminderStatus.PENDING);

        Reminder entityAfterSave = new Reminder(
                1L, "test1", "test1", localDateTime, user_id, ReminderStatus.PENDING);

        CreateReminderRequest reminderRequest = new CreateReminderRequest(
                "test1","test1",requestTime);

        ReminderResponse reminderResponse = new ReminderResponse(
                1L,"test1","test1",localDateTime, user_id);


        when(provider.getUser_id()).thenReturn(user_id);
        when(mapper.dtoToEntity(reminderRequest, user_id)).thenReturn(entityBeforeSave);
        when(repo.save(entityBeforeSave)).thenReturn(entityAfterSave);
        when(mapper.entityToResponse(entityAfterSave)).thenReturn(reminderResponse);

        ReminderResponse result = reminderService.create(reminderRequest);

        assertEquals(reminderResponse, result);

        verify(provider).getUser_id();
        verify(mapper).dtoToEntity(reminderRequest, user_id);
        verify(repo).save(entityBeforeSave);
        verify(mapper).entityToResponse(entityAfterSave);
        verify(reminderSchedulerService).scheduleReminder(1L, localDateTime);
    }
}
