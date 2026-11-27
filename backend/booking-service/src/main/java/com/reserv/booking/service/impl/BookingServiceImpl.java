package com.reserv.booking.service.impl;

import com.reserv.booking.dto.BookingCreateRequest;
import com.reserv.booking.dto.BookingHistoryResponse;
import com.reserv.booking.dto.BookingResponse;
import com.reserv.booking.dto.BookingUpdateRequest;
import com.reserv.booking.entity.Booking;
import com.reserv.booking.entity.BookingHistory;
import com.reserv.booking.entity.BookingStatus;
import com.reserv.booking.event.BookingEventPublisher;
import com.reserv.booking.exception.BookingConflictException;
import com.reserv.booking.exception.InvalidBookingException;
import com.reserv.booking.exception.ResourceNotFoundException;
import com.reserv.booking.repository.BookingRepository;
import com.reserv.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implémentation de la logique métier du Booking Service.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private static final Map<BookingStatus, Set<BookingStatus>> ALLOWED_TRANSITIONS = createAllowedTransitions();

    private final BookingRepository bookingRepository;
    private final BookingEventPublisher eventPublisher;

    private static Map<BookingStatus, Set<BookingStatus>> createAllowedTransitions() {
        Map<BookingStatus, Set<BookingStatus>> transitions = new EnumMap<>(BookingStatus.class);
        transitions.put(BookingStatus.PENDING, EnumSet.of(BookingStatus.CONFIRMED, BookingStatus.CANCELLED));
        transitions.put(BookingStatus.CONFIRMED, EnumSet.of(BookingStatus.COMPLETED, BookingStatus.CANCELLED));
        transitions.put(BookingStatus.CANCELLED, EnumSet.noneOf(BookingStatus.class));
        transitions.put(BookingStatus.COMPLETED, EnumSet.noneOf(BookingStatus.class));
        return transitions;
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request) {
        validateBookingScheduleAndPrice(
                request.getBookingDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getTotalPrice()
        );
        ensureNoSlotOverlap(
                request.getProviderId(),
                request.getBookingDate(),
                request.getStartTime(),
                request.getEndTime(),
                null
        );

        Booking booking = Booking.builder()
                .clientId(request.getClientId())
                .providerId(request.getProviderId())
                .serviceId(request.getServiceId())
                .bookingDate(request.getBookingDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .totalPrice(request.getTotalPrice())
                .status(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        recordStatusChange(booking, null, BookingStatus.PENDING);

        Booking saved = bookingRepository.save(booking);
        eventPublisher.publishBookingCreated(saved);
        return toResponse(saved);
    }

    @Override
    public List<BookingResponse> getAllBookings() {
        return mapToResponseList(bookingRepository.findAll());
    }

    @Override
    public BookingResponse getBookingById(Long id) {
        return toResponse(findBookingOrThrow(id));
    }

    @Override
    public List<BookingResponse> getBookingsByClientId(Long clientId) {
        return mapToResponseList(bookingRepository.findByClientIdOrderByBookingDateDescStartTimeDesc(clientId));
    }

    @Override
    public List<BookingResponse> getBookingsByProviderId(Long providerId) {
        return mapToResponseList(bookingRepository.findByProviderIdOrderByBookingDateDescStartTimeDesc(providerId));
    }

    @Override
    @Transactional
    public BookingResponse updateBooking(Long id, BookingUpdateRequest request) {
        Booking booking = findBookingOrThrow(id);
        ensureBookingIsModifiable(booking);

        validateBookingScheduleAndPrice(
                request.getBookingDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getTotalPrice()
        );

        BookingStatus previousStatus = booking.getStatus();
        BookingStatus requestedStatus = request.getStatus() != null ? request.getStatus() : previousStatus;
        boolean statusChanged = requestedStatus != previousStatus;

        if (statusChanged) {
            validateStatusTransition(previousStatus, requestedStatus);
        }

        if (requestedStatus != BookingStatus.CANCELLED) {
            ensureNoSlotOverlap(
                    booking.getProviderId(),
                    request.getBookingDate(),
                    request.getStartTime(),
                    request.getEndTime(),
                    booking.getId()
            );
        }

        if (request.getServiceId() != null) {
            booking.setServiceId(request.getServiceId());
        }
        booking.setBookingDate(request.getBookingDate());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setTotalPrice(request.getTotalPrice());

        if (statusChanged) {
            booking.setStatus(requestedStatus);
            recordStatusChange(booking, previousStatus, requestedStatus);
        }

        Booking saved = bookingRepository.save(booking);
        if (statusChanged) {
            publishStatusChangeEvent(saved, requestedStatus, null);
        }
        return toResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long id, String reason) {
        Booking booking = findBookingOrThrow(id);
        BookingStatus currentStatus = booking.getStatus();

        if (currentStatus == BookingStatus.CANCELLED) {
            throw new BookingConflictException("La réservation " + id + " est déjà annulée");
        }
        if (currentStatus == BookingStatus.COMPLETED) {
            throw new BookingConflictException("Impossible d'annuler une réservation déjà terminée");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        recordStatusChange(booking, currentStatus, BookingStatus.CANCELLED);

        Booking saved = bookingRepository.save(booking);
        eventPublisher.publishBookingCancelled(saved, reason);
        return toResponse(saved);
    }

    @Override
    public List<BookingResponse> getProviderPlanning(Long providerId, LocalDate date) {
        List<Booking> bookings = (date != null)
                ? bookingRepository.findByProviderIdAndBookingDateAndStatusNotOrderByStartTimeAsc(
                        providerId, date, BookingStatus.CANCELLED)
                : bookingRepository.findByProviderIdAndStatusNotOrderByBookingDateAscStartTimeAsc(
                        providerId, BookingStatus.CANCELLED);
        return mapToResponseList(bookings);
    }

    @Override
    public List<BookingResponse> getClientPlanning(Long clientId, LocalDate date) {
        List<Booking> bookings = (date != null)
                ? bookingRepository.findByClientIdAndBookingDateAndStatusNotOrderByStartTimeAsc(
                        clientId, date, BookingStatus.CANCELLED)
                : bookingRepository.findByClientIdAndStatusNotOrderByBookingDateAscStartTimeAsc(
                        clientId, BookingStatus.CANCELLED);
        return mapToResponseList(bookings);
    }

    private Booking findBookingOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable avec l'id : " + id));
    }

    private void validateBookingScheduleAndPrice(
            LocalDate bookingDate,
            LocalTime startTime,
            LocalTime endTime,
            BigDecimal totalPrice
    ) {
        if (bookingDate == null || bookingDate.isBefore(LocalDate.now())) {
            throw new InvalidBookingException("La date de réservation ne doit pas être dans le passé");
        }
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new InvalidBookingException("L'heure de fin doit être postérieure à l'heure de début");
        }
        if (totalPrice == null || totalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidBookingException("Le prix total doit être positif ou nul");
        }
    }

    private void ensureNoSlotOverlap(
            Long providerId,
            LocalDate bookingDate,
            LocalTime startTime,
            LocalTime endTime,
            Long excludeBookingId
    ) {
        boolean overlaps = bookingRepository.existsOverlappingBooking(
                providerId, bookingDate, startTime, endTime, excludeBookingId
        );
        if (overlaps) {
            throw new BookingConflictException(
                    "Le prestataire possède déjà une réservation active sur ce créneau horaire"
            );
        }
    }

    private void ensureBookingIsModifiable(Booking booking) {
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BookingConflictException(
                    "Impossible de modifier une réservation au statut " + booking.getStatus()
            );
        }
    }

    private void validateStatusTransition(BookingStatus currentStatus, BookingStatus targetStatus) {
        Set<BookingStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(targetStatus)) {
            throw new BookingConflictException(
                    "Transition de statut invalide de " + currentStatus + " vers " + targetStatus
            );
        }
    }

    private void recordStatusChange(Booking booking, BookingStatus oldStatus, BookingStatus newStatus) {
        BookingHistory entry = BookingHistory.builder()
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedAt(LocalDateTime.now())
                .build();
        booking.addHistory(entry);
    }

    private void publishStatusChangeEvent(Booking booking, BookingStatus newStatus, String reason) {
        switch (newStatus) {
            case CONFIRMED -> eventPublisher.publishBookingConfirmed(booking);
            case CANCELLED -> eventPublisher.publishBookingCancelled(booking, reason);
            case COMPLETED -> eventPublisher.publishBookingCompleted(booking);
            case PENDING -> {
                // Aucune transition vers PENDING n'est autorisée après création.
            }
        }
    }

    private List<BookingResponse> mapToResponseList(List<Booking> bookings) {
        return bookings.stream()
                .map(this::toResponse)
                .toList();
    }

    private BookingResponse toResponse(Booking booking) {
        List<BookingHistoryResponse> historyResponses = booking.getHistory() == null
                ? List.of()
                : booking.getHistory().stream()
                        .map(entry -> toHistoryResponse(entry, booking.getId()))
                        .toList();

        return BookingResponse.builder()
                .id(booking.getId())
                .clientId(booking.getClientId())
                .providerId(booking.getProviderId())
                .serviceId(booking.getServiceId())
                .bookingDate(booking.getBookingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .history(historyResponses)
                .build();
    }

    private BookingHistoryResponse toHistoryResponse(BookingHistory entry, Long bookingId) {
        return BookingHistoryResponse.builder()
                .id(entry.getId())
                .bookingId(entry.getBookingId() != null ? entry.getBookingId() : bookingId)
                .oldStatus(entry.getOldStatus())
                .newStatus(entry.getNewStatus())
                .changedAt(entry.getChangedAt())
                .build();
    }
}
