package com.lopatin.reminder.service;

import com.lopatin.reminder.api.request.CreateReminderRequest;
import com.lopatin.reminder.api.request.UpdateDto;
import com.lopatin.reminder.api.response.ReminderPageResponse;
import com.lopatin.reminder.api.response.ReminderResponse;
import com.lopatin.reminder.exception.ReminderNotFoundException;
import com.lopatin.reminder.mapper.UserProvider;
import com.lopatin.reminder.model.Reminder;
import com.lopatin.reminder.model.ReminderStatus;
import com.lopatin.reminder.repo.ReminderRepository;
import com.lopatin.reminder.scheduler.ReminderSchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Testcontainers
class ReminderServiceIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    ReminderService service;
    @Autowired
    ReminderRepository reminderRepository;


    @MockitoBean
    UserProvider userProvider;


    @MockitoBean
    ReminderSchedulerService schedulerService;

    @MockitoBean
    TelegramBotsApi telegramBotsApi;

    private final UUID userId = UUID.randomUUID();
    private final UUID userId2 = UUID.randomUUID();

    private final OffsetDateTime time = OffsetDateTime.parse("2030-03-27T13:31:10+03:00");

    @BeforeEach
            void setUp(){
        when(userProvider.getUser_id()).thenReturn(userId);

    }


    @Test
    void contextLoads(){}



    @Test
    public void create_createsReminder_shouldReturnReminderResponse() {

        CreateReminderRequest request =
                new CreateReminderRequest(
                "integration_test1",
                "integration_test1",
                time);

        ReminderResponse response = service.create(request);

        Optional<Reminder> saved = reminderRepository.findById(response.id());

        assertThat(saved).isPresent();
        assertThat(saved.get().getTitle()).isEqualTo("integration_test1");
        assertThat(saved.get().getUserId()).isEqualTo(userId);
        assertThat(saved.get().getStatus()).isEqualTo(ReminderStatus.PENDING);
    }

    @Test
    public void getAllReminders_shouldReturnOnlyOneReminderByOwnership(){
        Reminder reminder1 = Reminder.builder()
                .title("Test1")
                .remind(time.toLocalDateTime())
                .userId(userId)
                .status(ReminderStatus.PENDING)
                .build();
        Reminder reminder2 = Reminder.builder()
                .title("Test2")
                .remind(time.toLocalDateTime())
                .userId(userId2)
                .status(ReminderStatus.PENDING)
                .build();

        Sort sort = Sort.by("title").ascending();
        int page = 0;
        int size = 2;
        PageRequest pageable = PageRequest.of(page, size, sort);

        reminderRepository.save(reminder1);
        reminderRepository.save(reminder2);

        when(userProvider.getUser_id()).thenReturn(userId);

        ReminderPageResponse result = service
                .getAllReminders(null, null, null, pageable);

        assertThat(result.current()).hasSize(1);
        assertThat(result.current().getFirst().user_id()).isEqualTo(userId);
    }

    @Test
    public void getAllReminders_shouldReturnOnlyOneReminderBySearch(){
        Reminder reminder1 = Reminder.builder()
                .title("Test1")
                .remind(time.toLocalDateTime())
                .userId(userId)
                .status(ReminderStatus.PENDING)
                .build();
        Reminder reminder2 = Reminder.builder()
                .title("Test2")
                .remind(time.toLocalDateTime())
                .userId(userId)
                .status(ReminderStatus.PENDING)
                .build();

        Sort sort = Sort.by("title").ascending();
        int page = 0;
        int size = 2;
        PageRequest pageable = PageRequest.of(page, size, sort);

        reminderRepository.save(reminder1);
        reminderRepository.save(reminder2);

        when(userProvider.getUser_id()).thenReturn(userId);

        ReminderPageResponse result = service
                .getAllReminders("Test1", null, null, pageable);

        assertThat(result.current()).hasSize(1);
        assertThat(result.current().getFirst().title()).isEqualTo("Test1");
    }

    @Test
    public void getAllReminders_shouldReturnRemindersByDateRange(){
        Reminder reminder1 = Reminder.builder()
                .title("Test1")
                .remind(time.toLocalDateTime())
                .userId(userId)
                .status(ReminderStatus.PENDING)
                .build();
        Reminder reminder2 = Reminder.builder()
                .title("Test2")
                .remind(LocalDateTime.parse("2035-03-27T13:31:10"))
                .userId(userId)
                .status(ReminderStatus.PENDING)
                .build();

        Sort sort = Sort.by("title").ascending();
        int page = 0;
        int size = 2;
        PageRequest pageable = PageRequest.of(page, size, sort);

        LocalDate dateFrom = LocalDate.parse("2033-03-27");
        LocalDate dateTo = LocalDate.parse("2036-03-27");

        reminderRepository.save(reminder1);
        reminderRepository.save(reminder2);

        when(userProvider.getUser_id()).thenReturn(userId);

        ReminderPageResponse result = service
                .getAllReminders(null, dateFrom, dateTo, pageable);

        assertThat(result.current()).hasSize(1);
        assertThat(result.current().getFirst().remind()).isEqualTo(LocalDateTime.parse("2035-03-27T13:31:10"));
    }


    @Test
    public void removeReminderById_shouldRemoveAndReturn1(){

        Reminder reminder1 = Reminder.builder()
                .title("Test1")
                .remind(time.toLocalDateTime())
                .userId(userId)
                .status(ReminderStatus.PENDING)
                .build();

        var saved = reminderRepository.save(reminder1);
        Long id = saved.getId();
        when(userProvider.getUser_id()).thenReturn(userId);

        service.removeReminderById(id);

        Optional<Reminder> result = reminderRepository.findByIdAndUserId(id, userId);

        assertThat(result).isNotPresent();
    }


    @Test
    public void removeReminderById_shouldThrowReminderNotFoundException(){
        assertThrows(ReminderNotFoundException.class, () ->
                service.removeReminderById(666L));
    }



    @Test
    public void editReminderById_shouldReturnEditedResponse(){
        Reminder reminder1 = Reminder.builder()
                .title("Before Edit")
                .remind(time.toLocalDateTime())
                .userId(userId)
                .status(ReminderStatus.PENDING)
                .build();

        UpdateDto dataToChange =
                new UpdateDto("Changed Title",
                        "Changed Description",
                        time.plusMonths(1));


        var saved = reminderRepository.save(reminder1);

        ReminderResponse result = service.editReminderById(saved.getId(),dataToChange);

        assertThat(result.title()).isEqualTo(dataToChange.title());
    }



    @Test
    public void editReminderById_shouldThrowReminderNotFoundException(){
        UpdateDto dataToChange = new UpdateDto(null, null, null);
        assertThrows(ReminderNotFoundException.class, () ->
                service.editReminderById(666L, dataToChange));
    }



}