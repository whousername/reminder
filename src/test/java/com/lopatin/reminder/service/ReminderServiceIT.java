package com.lopatin.reminder.service;

import com.lopatin.reminder.api.request.CreateReminderRequest;
import com.lopatin.reminder.api.response.ReminderResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReminderServiceIT {

    @Autowired
    ReminderService service;


    @Test
    void contextLoads(){}

    @Test
    void create_createsReminder_shouldReturnReminderResponse() {

        LocalDateTime time = LocalDateTime.parse("2030-03-27T13:31:10");
        CreateReminderRequest request = new CreateReminderRequest(
                "integration_test1",
                "integration_test1",
                time,
                1);

        ReminderResponse response = service.create(request);

        assertNotNull(response.id());
    }
}