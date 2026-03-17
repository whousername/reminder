package com.lopatin.reminder.scheduler;

import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class ReminderSchedulerService {

    private Scheduler scheduler;

    public ReminderSchedulerService(Scheduler scheduler){
        this.scheduler = scheduler;
    }


    public void scheduleReminder(Long id, LocalDateTime remind)  {

        JobDetail jobDetail = JobBuilder.newJob(ReminderJob.class)
                .withIdentity("reminder:  " + id)
                .usingJobData("reminderId", String.valueOf(id))
                .storeDurably()
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .startAt(Date.from(remind
                        .atZone(ZoneId.systemDefault())
                        .toInstant()))
                .withIdentity("trigger: " + id)
                .build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to schedule reminder " + id, e); //надо как то по другому обернуть

        }
    }


    //надо будет 3 метода ?
    //    scheduleReminder()
    //    deleteReminderSchedule()
    //    rescheduleReminder()
}
