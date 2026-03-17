package com.lopatin.reminder.scheduler;

import com.lopatin.reminder.service.NotificationService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.management.Notification;
import java.util.UUID;

@Component
public class ReminderJob implements Job {

    @Autowired
    private NotificationService notificationService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Long reminderId = Long.valueOf(
                context.getMergedJobDataMap().getString("reminderId")
        );

        notificationService.sendReminder(reminderId);
    }
}
