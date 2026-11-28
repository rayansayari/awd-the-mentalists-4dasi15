package com.reserv.review.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration RabbitMQ du Review Service : exchange partagé awd.events,
 * file dédiée à booking.completed et convertisseur JSON.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "awd.events";
    public static final String BOOKING_COMPLETED_QUEUE = "review.booking-completed";
    public static final String BOOKING_COMPLETED_KEY = "booking.completed";
    public static final String RATING_UPDATED_KEY = "review.rating-updated";

    @Bean
    public TopicExchange awdEventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue bookingCompletedQueue() {
        return QueueBuilder.durable(BOOKING_COMPLETED_QUEUE).build();
    }

    @Bean
    public Binding bookingCompletedBinding(Queue bookingCompletedQueue, TopicExchange awdEventsExchange) {
        return BindingBuilder.bind(bookingCompletedQueue).to(awdEventsExchange).with(BOOKING_COMPLETED_KEY);
    }

    /**
     * Le type cible est déduit du paramètre du listener (et non de l'en-tête __TypeId__),
     * car la classe d'événement du Booking Service n'existe pas dans ce microservice.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}
