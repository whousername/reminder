package com.lopatin.reminder.api.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record UpdateDto(

        @Size(max = 255)
        String title,

        @Size(max = 4096)
        String description,

        @Future
        OffsetDateTime remind
)
{
}
