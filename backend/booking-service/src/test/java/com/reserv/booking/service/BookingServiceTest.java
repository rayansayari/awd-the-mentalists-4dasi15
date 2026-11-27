package com.reserv.booking.service;

import com.reserv.booking.dto.BookingCreateRequest;
import com.reserv.booking.dto.BookingResponse;
import com.reserv.booking.entity.Booking;
import com.reserv.booking.entity.BookingStatus;
import com.reserv.booking.event.BookingEventPublisher;
import com.reserv.booking.exception.BookingConflictException;
import com.reserv.booking.exception.InvalidBookingException;
import com.reserv.booking.repository.BookingRepository;
import com.reserv.booking.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingEventPublisher eventPublisher;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Booking sampleBooking;

    @BeforeEach
    void setUp() {
        sampleBooking = Booking.builder()
                .id(1L)
                .clientId(10L)
                .providerId(20L)
                .serviceId(30L)
                .bookingDate(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .totalPrice(new BigDecimal("50.000"))
                .status(BookingStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Création d'une réservation valide")
    void testCreateBookingSuccess() {
        BookingCreateRequest request = BookingCreateRequest.builder()
                .clientId(10L)
                .providerId(20L)
                .serviceId(30L)
                .bookingDate(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .totalPrice(new BigDecimal("50.000"))
                .build();

        when(bookingRepository.existsOverlappingBooking(any(), any(), any(), any(), any())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(1L);
            return b;
        });

        BookingResponse response = bookingService.createBooking(request);

        assertNotNull(response);
        assertEquals(BookingStatus.PENDING, response.getStatus());
        verify(eventPublisher).publishBookingCreated(any(Booking.class));
    }

    @Test
    @DisplayName("Refus de création avec heure de fin antérieure à l'heure de début")
    void testCreateBookingInvalidTimes() {
        BookingCreateRequest request = BookingCreateRequest.builder()
                .clientId(10L)
                .providerId(20L)
                .serviceId(30L)
                .bookingDate(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(10, 0))
                .totalPrice(new BigDecimal("50.000"))
                .build();

        assertThrows(InvalidBookingException.class, () -> bookingService.createBooking(request));
    }

    @Test
    @DisplayName("Annulation avec succès et publication d'événement")
    void testCancelBookingSuccess() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(sampleBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.cancelBooking(1L, "Empêchement");

        assertEquals(BookingStatus.CANCELLED, response.getStatus());
        verify(eventPublisher).publishBookingCancelled(any(Booking.class), any());
    }

    @Test
    @DisplayName("Refus d'annulation si déjà terminée")
    void testCancelBookingAlreadyCompleted() {
        sampleBooking.setStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(sampleBooking));

        assertThrows(BookingConflictException.class, () -> bookingService.cancelBooking(1L, "Raison"));
    }
}
