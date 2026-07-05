package com.lopatin.reminder.api.request;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record CreateReminderRequest (

        @NotBlank
        @Size(max = 255)
        String title,

        @Size(max = 4096)
        String description,

        @NotNull
        @Future
        OffsetDateTime remind
)
{
}
