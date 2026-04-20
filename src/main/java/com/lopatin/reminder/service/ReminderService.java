package com.lopatin.reminder.service;

import com.lopatin.reminder.api.request.CreateReminderRequest;
import com.lopatin.reminder.api.request.UpdateDto;
import com.lopatin.reminder.api.response.ReminderPageResponse;
import com.lopatin.reminder.exception.ReminderNotFoundException;
import com.lopatin.reminder.mapper.ReminderMapper;
import com.lopatin.reminder.api.response.ReminderResponse;
import com.lopatin.reminder.mapper.UserProvider;
import com.lopatin.reminder.model.Reminder;
import com.lopatin.reminder.repo.ReminderRepository;
import com.lopatin.reminder.scheduler.ReminderSchedulerService;
import com.lopatin.reminder.service.specification.ReminderSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ReminderService {


    private final ReminderMapper mapper;
    private final  ReminderRepository reminderRepo;
    private final UserProvider userProvider;
    private final ReminderSchedulerService schedulerService;


    public ReminderService
            (ReminderMapper mapper,
             ReminderRepository reminderRepo,
             UserProvider userProvider,
             ReminderSchedulerService schedulerService)
    {
        this.mapper = mapper;
        this.reminderRepo = reminderRepo;
        this.userProvider = userProvider;
        this.schedulerService = schedulerService;
    }


    @Transactional
    public ReminderResponse create(CreateReminderRequest request){
        UUID userId = userProvider.getUser_id();
        return create(request, userId);
    }

    @Transactional //перегрузка для бота
    public ReminderResponse create(CreateReminderRequest request, UUID userId){
        log.info("Creating reminder for userId={}", userId);

        var savedReminder = reminderRepo.save(mapper.dtoToEntity(request, userId));

        if(TransactionSynchronizationManager.isSynchronizationActive()){
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            schedulerService
                                    .scheduleReminder(
                                            savedReminder.getId(),
                                            savedReminder.getRemind());
                        }});
        } else { //no transaction
            schedulerService
                    .scheduleReminder(
                            savedReminder.getId(),
                            savedReminder.getRemind());
        }
        return mapper.entityToResponse(savedReminder);
    }


    public ReminderPageResponse getAllReminders(
            String search,
            LocalDate dateFrom,
            LocalDate dateTo,
            Pageable pageable) {

        UUID currentUser = userProvider.getUser_id();

        Specification<Reminder> specification = Specification
                .where(ReminderSpec.byUserId(currentUser))
                .and(ReminderSpec.bySearch(search))
                .and(ReminderSpec.byDateRange(dateFrom, dateTo));

        Page<Reminder> page = reminderRepo.findAll(specification, pageable);

        List<ReminderResponse> content = page.getContent()
                .stream()
                .map(mapper::entityToResponse)
                .toList();

        return new ReminderPageResponse(
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                content);
    }


    //перегрузка для бота
    public List<ReminderResponse> getAllReminders(UUID userId, Pageable pageable){
        return reminderRepo
                .findAllByUserId(userId)
                .stream()
                .map(mapper::entityToResponse)
                .toList();
    }



    @Transactional
    public void removeReminderById(Long id) {
        UUID currentUser = userProvider.getUser_id();
        int deleted = reminderRepo.deleteByIdAndUserId(id, currentUser);
        if(deleted == 0){
            throw new ReminderNotFoundException(id);
        }
        log.info("Reminder={} successfully deleted for userId={}", id, currentUser);
    }

    //перегрузка для бота
    @Transactional
    public void removeReminderById(UUID userId, Long id) {
        int deleted = reminderRepo.deleteByIdAndUserId(id, userId);
        if(deleted == 0){
            throw new ReminderNotFoundException(id);
        }
        log.info("Reminder={} successfully deleted for userId={}", id, userId);

    }


    @Transactional
    public ReminderResponse editReminderById(Long id, UpdateDto dataToChange) {
        UUID currentUser = userProvider.getUser_id();
        return editReminderById(currentUser, id, dataToChange);
    }

    //перегрузка для бота
    @Transactional
    public ReminderResponse editReminderById(UUID userId, Long reminderId, UpdateDto dataToChange) {

        Reminder reminder = reminderRepo.findByIdAndUserId(reminderId, userId)
                .orElseThrow(() -> new ReminderNotFoundException(reminderId));

        if (dataToChange.title() != null) {
            reminder.setTitle(dataToChange.title());
        }
        if (dataToChange.description() != null) {
            reminder.setDescription(dataToChange.description());
        }
        if (dataToChange.remind() != null) {
            reminder.setRemind(dataToChange
                    .remind()
                    .withOffsetSameInstant(ZoneOffset.UTC)
                    .toLocalDateTime());
        }
        log.info("Reminder updated: id={}, userId={}",
                reminderId, userId);
        return mapper.entityToResponse(reminder);
    }

}
