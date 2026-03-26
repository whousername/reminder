package com.lopatin.reminder.scheduler;

import com.lopatin.reminder.model.ReminderStatus;
import com.lopatin.reminder.repo.ReminderRepository;
import com.lopatin.reminder.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Slf4j
@Component
public class ReminderJob implements Job {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ReminderRepository reminderRepo;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Long reminderId = Long.valueOf(
                context.getMergedJobDataMap().getString("reminderId")
        );

        var reminder = reminderRepo.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found!"));
        LocalDateTime remindAt = reminder.getRemind();

        //misfire
        if (remindAt.isBefore(LocalDateTime.now().minusMinutes(5))){
            log.warn("Reminder={} is outdated, skipping.", reminderId);
            reminder.setStatus(ReminderStatus.FAILED);
            reminderRepo.save(reminder);
            return;
        }

        notificationService.sendReminder(reminderId);
    }
}
