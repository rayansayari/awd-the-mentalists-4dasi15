package com.reserv.review.event;

import com.reserv.review.config.RabbitConfig;
import com.reserv.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Consomme booking.completed (exchange awd.events) : une prestation réalisée autorise l'avis du client.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventListener {

    private static final String BOOKING_COMPLETED = "BookingCompleted";

    private final ReviewService reviewService;

    @RabbitListener(queues = RabbitConfig.BOOKING_COMPLETED_QUEUE)
    public void onBookingCompleted(ReviewEvent event) {
        if (event == null || event.getData() == null) {
            log.warn("Événement booking.completed ignoré : message vide");
            return;
        }
        if (event.getEventType() != null && !BOOKING_COMPLETED.equals(event.getEventType())) {
            log.warn("Événement ignoré : type inattendu {}", event.getEventType());
            return;
        }

        Map<String, Object> data = event.getData();
        Long bookingId = toLong(data.get("bookingId"));
        Long clientId = toLong(data.get("clientId"));
        Long providerId = toLong(data.get("providerId"));

        if (bookingId == null || clientId == null || providerId == null) {
            log.warn("Événement booking.completed ignoré : champs manquants (eventId={}, data={})",
                    event.getEventId(), data);
            return;
        }

        reviewService.registerCompletedBooking(event.getEventId(), bookingId, clientId, providerId);
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Long.parseLong(text.trim());
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }
}
