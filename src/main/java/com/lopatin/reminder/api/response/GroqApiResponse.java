package com.lopatin.reminder.api.response;

import java.util.List;

public record GroqApiResponse(
        List<GroqChoice> choices
) {
}
