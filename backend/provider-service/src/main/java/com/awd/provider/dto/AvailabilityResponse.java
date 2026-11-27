package com.awd.provider.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record AvailabilityResponse(Long id, Long providerId, DayOfWeek dayOfWeek,
                                   LocalTime startTime, LocalTime endTime, Boolean isAvailable) {}
