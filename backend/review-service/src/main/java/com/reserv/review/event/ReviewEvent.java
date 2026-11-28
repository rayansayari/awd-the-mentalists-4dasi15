package com.reserv.review.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Enveloppe standard d'événement asynchrone conforme aux conventions RE'SERV ({eventId, eventType, data}).
 * Utilisée à la fois pour publier (review.rating-updated) et consommer (booking.completed).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewEvent {

    private String eventId;
    private String eventType;
    private Map<String, Object> data;
}
