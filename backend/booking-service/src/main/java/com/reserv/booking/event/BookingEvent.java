package com.reserv.booking.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Enveloppe standard d'événement asynchrone conforme aux conventions RE'SERV ({eventId, eventType, data}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingEvent {

    private String eventId;
    private String eventType;
    private Map<String, Object> data;
}
