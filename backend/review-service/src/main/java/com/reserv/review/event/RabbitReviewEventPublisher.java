package com.reserv.review.event;

import com.reserv.review.config.RabbitConfig;
import com.reserv.review.entity.ProviderRatingSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Publie les événements du Review Service sur l'exchange awd.events.
 * Si RabbitMQ est indisponible, l'erreur est journalisée sans faire échouer la requête REST.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitReviewEventPublisher implements ReviewEventPublisher {

    private final AmqpTemplate amqpTemplate;

    @Override
    public void publishRatingUpdated(ProviderRatingSummary summary) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("providerId", summary.getProviderId());
        data.put("averageRating", summary.getAverageRating());
        data.put("ratingCount", summary.getRatingCount());

        ReviewEvent event = ReviewEvent.builder()
                .eventId("evt-" + UUID.randomUUID())
                .eventType("ReviewRatingUpdated")
                .data(data)
                .build();

        try {
            amqpTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.RATING_UPDATED_KEY, event);
            log.info("Événement publié [routingKey={}, eventId={}, data={}]",
                    RabbitConfig.RATING_UPDATED_KEY, event.getEventId(), event.getData());
        } catch (Exception ex) {
            log.error("Échec de publication de l'événement {} (eventId={}) : {}",
                    event.getEventType(), event.getEventId(), ex.getMessage());
        }
    }
}
