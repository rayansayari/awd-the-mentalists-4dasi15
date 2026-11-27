package com.awd.provider.dto;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

/** JSON: {"dayOfWeek":"MONDAY","startTime":"08:00","endTime":"12:00","isAvailable":true} */
public record AvailabilityRequest(
        @NotNull(message = "dayOfWeek est obligatoire") DayOfWeek dayOfWeek,
        @NotNull(message = "startTime est obligatoire") LocalTime startTime,
        @NotNull(message = "endTime est obligatoire") LocalTime endTime,
        @NotNull(message = "isAvailable est obligatoire") Boolean isAvailable
) {}
