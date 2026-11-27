package com.reserv.booking.event;

import com.reserv.booking.entity.Booking;

/**
 * Contrat de publication des événements métier du Booking Service (préparation RabbitMQ).
 */
public interface BookingEventPublisher {

    void publishBookingCreated(Booking booking);

    void publishBookingConfirmed(Booking booking);

    void publishBookingCancelled(Booking booking, String reason);

    void publishBookingCompleted(Booking booking);
}
