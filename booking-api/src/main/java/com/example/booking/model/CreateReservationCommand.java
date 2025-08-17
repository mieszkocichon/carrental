package com.example.booking.model;
import java.time.Instant;
import java.util.UUID;

public record CreateReservationCommand(
    String type,
    UUID reservationId,
    UUID customerId,
    UUID vehicleId,
    Instant pickupTime,
    Instant returnTime,
    Instant requestedAt
) { }
