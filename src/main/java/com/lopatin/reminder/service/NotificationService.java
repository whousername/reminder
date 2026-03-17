package com.lopatin.reminder.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {


    public void sendReminder(Long reminderId) {

        System.out.println("NotificationService.sendReminder: REMINDER FIRED=id: " + reminderId);
        //        1 найти ремайндер в репо
        //        2 отправить email emailservice
        //        3 отправить telegram telegramservice
        //        4 отметить reminder как выполненный
    }
}
