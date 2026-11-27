package com.reserv.booking.service;

import com.reserv.booking.dto.BookingCreateRequest;
import com.reserv.booking.dto.BookingResponse;
import com.reserv.booking.dto.BookingUpdateRequest;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface métier pour la gestion des réservations et des plannings.
 */
public interface BookingService {

    BookingResponse createBooking(BookingCreateRequest request);

    List<BookingResponse> getAllBookings();

    BookingResponse getBookingById(Long id);

    List<BookingResponse> getBookingsByClientId(Long clientId);

    List<BookingResponse> getBookingsByProviderId(Long providerId);

    BookingResponse updateBooking(Long id, BookingUpdateRequest request);

    BookingResponse cancelBooking(Long id, String reason);

    List<BookingResponse> getProviderPlanning(Long providerId, LocalDate date);

    List<BookingResponse> getClientPlanning(Long clientId, LocalDate date);
}
