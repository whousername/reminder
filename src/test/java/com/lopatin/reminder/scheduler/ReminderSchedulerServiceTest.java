package com.lopatin.reminder.scheduler;

import com.lopatin.reminder.exception.ReminderSchedulingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderSchedulerServiceTest {

    @Mock
    private Scheduler scheduler;

    @InjectMocks
    private ReminderSchedulerService reminderSchedulerService;

    private final Long id = 1L;
    private final LocalDateTime remind = LocalDateTime.parse("2030-03-27T13:31:10");


    @Test
    void scheduleReminder_shouldCreateScheduleForReminderWithIdAndRemind() throws SchedulerException {
        reminderSchedulerService.scheduleReminder(id, remind);
        verify(scheduler).scheduleJob(any(JobDetail.class),any(Trigger.class));

    }

    @Test
    void scheduleReminder_shouldThrowReminderSchedulingException() throws SchedulerException {
        doThrow( new SchedulerException())
                .when(scheduler).scheduleJob(any(JobDetail.class),any(Trigger.class));

        assertThrows(ReminderSchedulingException.class, () ->
                reminderSchedulerService.scheduleReminder(id,remind));
    }

}