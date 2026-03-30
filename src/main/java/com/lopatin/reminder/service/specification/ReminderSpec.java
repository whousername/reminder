package com.lopatin.reminder.service.specification;

import com.lopatin.reminder.model.Reminder;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class ReminderSpec {


    public static Specification<Reminder> byUserId(UUID userId){
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("userId"),userId));
    }


    public static Specification<Reminder> bySearch(String search){

        if (search == null || search.isBlank()){
            return null;
        }
        String pattern = "%" + search.toLowerCase() + "%";

        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern))
        );
    }


    public static Specification<Reminder> byDateRange (LocalDate dateFrom, LocalDate dateTo){

        return ((root, query, criteriaBuilder) ->
        {
            if (dateTo == null && dateFrom == null) { return null; }

            if (dateFrom == null){
                return criteriaBuilder
                        .lessThanOrEqualTo(root.get("remind"), dateTo.atTime(23,59));
            }
            if (dateTo == null){
                return criteriaBuilder
                        .greaterThanOrEqualTo(root.get("remind"), dateFrom.atStartOfDay());
            }
            return criteriaBuilder.between(
                    root.get("remind"),
                    dateFrom.atStartOfDay(),
                    dateTo.atTime(23,59));
        });
    }


}
