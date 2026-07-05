package com.lopatin.reminder.api.request;

import com.lopatin.reminder.api.dto.GroqMessage;
import java.util.List;

public record GroqRequest(
        String model,
        List<GroqMessage> messages
) {
}
