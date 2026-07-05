package com.lopatin.reminder.api.response;

import java.util.List;

public record ReminderPageResponse(
        long total,
        long page,
        long size,
        List<ReminderResponse> current) {

}
