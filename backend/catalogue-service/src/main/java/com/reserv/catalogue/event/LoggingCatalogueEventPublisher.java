package com.reserv.catalogue.event;

import com.reserv.catalogue.entity.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation par defaut journalisant les evenements du catalogue
 * (en attente du branchement effectif du broker RabbitMQ sur l'exchange {@code awd.events}).
 */
@Slf4j
@Component
public class LoggingCatalogueEventPublisher implements CatalogueEventPublisher {

    @Override
    public void publishServiceCreated(Service service) {
        emit("ServiceCreated", buildServicePayload(service));
    }

    @Override
    public void publishServiceUpdated(Service service) {
        emit("ServiceUpdated", buildServicePayload(service));
    }

    @Override
    public void publishServiceDesactivated(Service service) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("serviceId", service.getId());
        data.put("status", service.getStatus());
        emit("ServiceDesactivated", data);
    }

    @Override
    public void publishServiceDeleted(Long serviceId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("serviceId", serviceId);
        emit("ServiceDeleted", data);
    }

    private Map<String, Object> buildServicePayload(Service service) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("serviceId", service.getId());
        data.put("name", service.getName());
        data.put("price", service.getPrice());
        data.put("duration", service.getDuration());
        data.put("categoryId", service.getCategoryId());
        data.put("status", service.getStatus());
        return data;
    }

    private void emit(String eventType, Map<String, Object> data) {
        CatalogueEvent event = CatalogueEvent.builder()
                .eventId("evt-" + UUID.randomUUID())
                .eventType(eventType)
                .data(data)
                .build();
        log.info("Evenement catalogue prepare pour awd.events [eventType={}, eventId={}, data={}]",
                event.getEventType(), event.getEventId(), event.getData());
    }
}
