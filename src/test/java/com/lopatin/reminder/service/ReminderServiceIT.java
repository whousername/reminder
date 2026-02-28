package com.lopatin.reminder.service;

import com.lopatin.reminder.api.request.CreateReminderRequest;
import com.lopatin.reminder.api.response.ReminderResponse;
import com.lopatin.reminder.mapper.UserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReminderServiceIT {

    @Autowired
    ReminderService service;

    @MockitoBean
    UserProvider userProvider;


    @Test
    void contextLoads(){}

    @Test
    void create_createsReminder_shouldReturnReminderResponse() {

        UUID user_id = UUID.randomUUID();
        LocalDateTime time = LocalDateTime.parse("2030-03-27T13:31:10");
        CreateReminderRequest request = new CreateReminderRequest(
                "integration_test1",
                "integration_test1",
                time);
        when(userProvider.getUser_id()).thenReturn(user_id);

        ReminderResponse response = service.create(request);

        assertNotNull(response.id());
    }
}