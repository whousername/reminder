package com.lopatin.reminder.service;


import com.lopatin.reminder.api.request.CreateReminderRequest;
import com.lopatin.reminder.api.dto.UpdateDto;
import com.lopatin.reminder.api.response.ReminderPageResponse;
import com.lopatin.reminder.api.response.ReminderResponse;
import com.lopatin.reminder.exception.ReminderNotFoundException;
import com.lopatin.reminder.mapper.ReminderMapper;
import com.lopatin.reminder.mapper.UserProvider;
import com.lopatin.reminder.model.Reminder;
import com.lopatin.reminder.model.ReminderProgress;
import com.lopatin.reminder.model.ReminderStatus;
import com.lopatin.reminder.repo.ReminderRepository;
import com.lopatin.reminder.scheduler.ReminderSchedulerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clearSynchronization();
    }



    @Test
    public void createReminder_shouldMapSaveAndReturnResponse() {

        OffsetDateTime requestTime = OffsetDateTime.parse(
                "2030-03-27T13:31:10Z");

        LocalDateTime localDateTime = LocalDateTime.parse(
                "2030-03-27T13:31:10");

        UUID user_id = UUID.randomUUID();

        Reminder entityBeforeSave = new Reminder(
                null, "test1", "test1", localDateTime, user_id, ReminderStatus.PENDING, null);

        Reminder entityAfterSave = new Reminder(
                1L, "test1", "test1", localDateTime, user_id, ReminderStatus.PENDING, ReminderProgress.CREATED);

        CreateReminderRequest reminderRequest = new CreateReminderRequest(
                "test1","test1",requestTime);

        ReminderResponse reminderResponse = new ReminderResponse(
                1L,"test1","test1",localDateTime, user_id, ReminderProgress.CREATED);


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

    }

    @Test
    public void getAllReminders_shouldReturnReminderPageResponse(){
        String search = "test2";
        LocalDate dateFrom = LocalDate.parse("2030-01-01");
        LocalDate dateTo = LocalDate.parse("2030-01-02");
        LocalDateTime remindDate = LocalDateTime.parse(
                "2030-01-01T13:30:00");
        String sortBy = "title";
        String direction = "asc";
        int page = 0;
        int size = 10;

        UUID currentUser = UUID.randomUUID();

        Reminder reminder = Reminder.builder()
                .id(1L)
                .title("Test2")
                .remind(remindDate)
                .userId(currentUser)
                .status(ReminderStatus.PENDING)
                .build();

        ReminderResponse reminderResponse = new ReminderResponse(
                1L,"Test2", null, remindDate,currentUser, ReminderProgress.CREATED);

        Page<Reminder> reminderPage = new PageImpl<Reminder>(List.of(reminder));

        when(provider.getUser_id()).thenReturn(currentUser);
        when(repo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(reminderPage);
        when(mapper.entityToResponse(any(Reminder.class))).thenReturn(reminderResponse);

        ReminderPageResponse result = reminderService.getAllReminders(search, dateFrom, dateTo, sortBy, direction, page, size);

        verify(provider).getUser_id();
        verify(repo).findAll(any(Specification.class), any(Pageable.class));
        verify(mapper).entityToResponse(any(Reminder.class));

        assertThat(result.current()).contains(reminderResponse);
    }

    @Test
    public void removeReminderById_shouldRemoveReminder(){
        Long id = 1L;
        UUID currentUser = UUID.randomUUID();

        when(provider.getUser_id()).thenReturn(currentUser);
        when(repo.deleteByIdAndUserId(any(Long.class), any(UUID.class))).thenReturn(1);

        reminderService.removeReminderById(id);

        verify(provider).getUser_id();
        verify(repo).deleteByIdAndUserId(any(Long.class), any(UUID.class));
    }

    @Test
    public void removeReminderById_shouldThrowReminderNotFoundException() {
        Long id = 1L;
        UUID currentUser = UUID.randomUUID();

        when(provider.getUser_id()).thenReturn(currentUser);
        when(repo.deleteByIdAndUserId(any(Long.class), any(UUID.class))).thenReturn(0);

        assertThrows(ReminderNotFoundException.class,
                () -> reminderService.removeReminderById(id));
    }

    @Test
    public void editReminderById_shouldUpdateTwoFieldsAndReturnReminderResponse(){
        Long id = 1L;
        UUID currentUser = UUID.randomUUID();
        UpdateDto updateDto = new UpdateDto(
                "titleToUpdate",
                null,
                OffsetDateTime.parse("2030-02-01T10:15:30+03:00"));

        Reminder reminder = Reminder.builder()
                .id(1L)
                .title("Test5")
                .description("Test5")
                .remind(LocalDateTime.parse("2030-01-01T13:30:00"))
                .userId(currentUser)
                .status(ReminderStatus.PENDING)
                .build();

        when(provider.getUser_id()).thenReturn(currentUser);
        when(repo.findByIdAndUserId(any(Long.class), any(UUID.class)))
                .thenReturn(Optional.of(reminder));
        when(mapper.entityToResponse(any(Reminder.class)))
                .thenAnswer(inv -> {
            Reminder r = inv.getArgument(0);
            return new ReminderResponse(
                    r.getId(),
                    r.getTitle(), r.getDescription(),
                    r.getRemind(), r.getUserId(), r.getProgress());
                });

        ReminderResponse result = reminderService.editReminderById(id, updateDto);

        assertThat(result.title()).isEqualTo("titleToUpdate");
        assertThat(result.description()).isEqualTo("Test5");
        assertThat(result.remind()).isEqualTo(LocalDateTime.parse("2030-02-01T07:15:30"));
    }

    @Test
    public void editReminderById_shouldThrowReminderNotFoundException(){
        Long id = 1L;
        UUID currentUser = UUID.randomUUID();
        UpdateDto updateDto = new UpdateDto(
                null,null, null);

        when(provider.getUser_id()).thenReturn(currentUser);
        when(repo.findByIdAndUserId(any(Long.class), any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(ReminderNotFoundException.class, () ->
                reminderService.editReminderById(id, updateDto));
    }



}
