package com.reserv.catalogue.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Enveloppe standard d'evenement asynchrone conforme aux conventions RE'SERV ({eventId, eventType, data}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogueEvent {

    private String eventId;
    private String eventType;
    private Map<String, Object> data;
}
