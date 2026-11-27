package com.awd.provider.mapper;

import com.awd.provider.dto.AvailabilityRequest;
import com.awd.provider.dto.AvailabilityResponse;
import com.awd.provider.entity.Availability;
import org.springframework.stereotype.Component;

@Component
public class AvailabilityMapper {
    public void apply(Availability a, AvailabilityRequest r) {
        a.setDayOfWeek(r.dayOfWeek());
        a.setStartTime(r.startTime());
        a.setEndTime(r.endTime());
        a.setAvailable(r.isAvailable());
    }
    public AvailabilityResponse toResponse(Availability a) {
        return new AvailabilityResponse(a.getId(), a.getProvider().getId(), a.getDayOfWeek(),
                a.getStartTime(), a.getEndTime(), a.getAvailable());
    }
}
