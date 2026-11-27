package com.reserv.booking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO utilisé lors de la création d'une réservation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCreateRequest {

    @NotNull(message = "L'identifiant du client est obligatoire")
    @Positive(message = "L'identifiant du client doit être positif")
    private Long clientId;

    @NotNull(message = "L'identifiant du prestataire est obligatoire")
    @Positive(message = "L'identifiant du prestataire doit être positif")
    private Long providerId;

    @NotNull(message = "L'identifiant du service est obligatoire")
    @Positive(message = "L'identifiant du service doit être positif")
    private Long serviceId;

    @NotNull(message = "La date de réservation est obligatoire")
    @FutureOrPresent(message = "La date de réservation ne doit pas être dans le passé")
    private LocalDate bookingDate;

    @NotNull(message = "L'heure de début est obligatoire")
    private LocalTime startTime;

    @NotNull(message = "L'heure de fin est obligatoire")
    private LocalTime endTime;

    @NotNull(message = "Le prix total est obligatoire")
    @DecimalMin(value = "0.0", inclusive = true, message = "Le prix total doit être positif ou nul")
    private BigDecimal totalPrice;
}
