package com.awd.provider.mapper;

import com.awd.provider.dto.InterventionZoneRequest;
import com.awd.provider.dto.InterventionZoneResponse;
import com.awd.provider.entity.InterventionZone;
import org.springframework.stereotype.Component;

@Component
public class InterventionZoneMapper {
    public void apply(InterventionZone z, InterventionZoneRequest r) {
        z.setCity(r.city());
        z.setRegion(r.region());
        z.setPostalCode(r.postalCode());
    }
    public InterventionZoneResponse toResponse(InterventionZone z) {
        return new InterventionZoneResponse(z.getId(), z.getProvider().getId(),
                z.getCity(), z.getRegion(), z.getPostalCode());
    }
}
