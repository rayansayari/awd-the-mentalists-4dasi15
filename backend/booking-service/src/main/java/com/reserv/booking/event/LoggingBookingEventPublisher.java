package com.reserv.booking.event;

import com.reserv.booking.entity.Booking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implémentation par défaut journalisant les événements de réservation avant le branchement effectif sur RabbitMQ.
 */
@Slf4j
@Component
public class LoggingBookingEventPublisher implements BookingEventPublisher {

    @Override
    public void publishBookingCreated(Booking booking) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("bookingId", booking.getId());
        data.put("clientId", booking.getClientId());
        data.put("providerId", booking.getProviderId());
        data.put("serviceId", booking.getServiceId());
        data.put("bookingDate", booking.getBookingDate());
        data.put("startTime", booking.getStartTime());
        data.put("endTime", booking.getEndTime());
        data.put("totalPrice", booking.getTotalPrice());
        data.put("status", booking.getStatus());
        emit("BookingCreated", data);
    }

    @Override
    public void publishBookingConfirmed(Booking booking) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("bookingId", booking.getId());
        data.put("providerId", booking.getProviderId());
        data.put("bookingDate", booking.getBookingDate());
        data.put("startTime", booking.getStartTime());
        data.put("endTime", booking.getEndTime());
        data.put("status", booking.getStatus());
        emit("BookingConfirmed", data);
    }

    @Override
    public void publishBookingCancelled(Booking booking, String reason) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("bookingId", booking.getId());
        data.put("clientId", booking.getClientId());
        data.put("providerId", booking.getProviderId());
        data.put("totalPrice", booking.getTotalPrice());
        data.put("status", booking.getStatus());
        data.put("reason", reason != null && !reason.isBlank() ? reason : "Annulation de la réservation");
        emit("BookingCancelled", data);
    }

    @Override
    public void publishBookingCompleted(Booking booking) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("bookingId", booking.getId());
        data.put("clientId", booking.getClientId());
        data.put("providerId", booking.getProviderId());
        data.put("serviceId", booking.getServiceId());
        data.put("status", booking.getStatus());
        emit("BookingCompleted", data);
    }

    private void emit(String eventType, Map<String, Object> data) {
        BookingEvent event = BookingEvent.builder()
                .eventId("evt-" + UUID.randomUUID())
                .eventType(eventType)
                .data(data)
                .build();
        log.info("Événement préparé [eventType={}, eventId={}, data={}]",
                event.getEventType(), event.getEventId(), event.getData());
    }
}
