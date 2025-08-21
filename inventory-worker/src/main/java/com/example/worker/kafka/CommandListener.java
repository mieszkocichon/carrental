package com.example.worker.kafka;

import com.example.worker.redis.AvailabilityCache;
import com.example.worker.redis.IdempotencyStore;
import com.example.worker.redis.VehicleLockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@Component
public class CommandListener {
  private static final Logger log = LoggerFactory.getLogger(CommandListener.class);
  private final ObjectMapper mapper = new ObjectMapper();
  private final IdempotencyStore idempotency;
  private final VehicleLockService locks;
  private final AvailabilityCache availability;

  public CommandListener(IdempotencyStore idempotency, VehicleLockService locks, AvailabilityCache availability) {
    this.idempotency = idempotency;
    this.locks = locks;
    this.availability = availability;
  }

  @KafkaListener(topics = "reservations.commands", groupId = "inventory-workers")
  public void onMessage(@Payload String payload) {
    try {
      Map<?,?> cmd = mapper.readValue(payload, Map.class);
      String type = String.valueOf(cmd.get("type"));
      if (!"CreateReservation".equals(type)) {
        log.info("skip_unsupported_command type={}", type);
        return;
      }
      String reservationId = String.valueOf(cmd.get("reservationId"));
      String vehicleId = String.valueOf(cmd.get("vehicleId"));

      // Idempotency on the worker side (handle a given reservationId only once)
      if (!idempotency.firstProcess(reservationId)) {
        log.info("duplicate_command_ignored reservationId={}", reservationId);
        return;
      }

      // Lock per vehicleId (we avoid a race between two reservations for the same car)
      String token = locks.tryLock(vehicleId, Duration.ofSeconds(10));
      if (token == null) {
        log.info("vehicle_locked_busy vehicleId={}", vehicleId);
        // Here you can publish ReservationRejected (reason=LOCKED) or try a retry.
        return;
      }

      try {
        // Check (from cache) if the car is available — placeholder
        var cached = availability.get(vehicleId).orElse("AVAILABLE");
        boolean available = cached.startsWith("AVAILABLE");

        if (available) {
          // Here, we would check the read model/database; on the MVP, we accept and “block” it in the cache.
          availability.put(vehicleId, "UNAVAILABLE", Duration.ofMinutes(5));
          log.info("reservation_accepted reservationId={} vehicleId={}", reservationId, vehicleId);
          // Krok 4/6: wyślemy ReservationAccepted na reservations.events
        } else {
          log.info("reservation_rejected_unavailable reservationId={} vehicleId={}", reservationId, vehicleId);
          // Krok 4/6: events -> ReservationRejected
        }
      } finally {
        try { locks.unlock(vehicleId, token); } catch (Exception e) {
          log.warn("unlock_failed vehicleId={} err={}", vehicleId, e.toString());
        }
      }
    } catch (Exception e) {
      log.error("command_processing_failed err={} payload={}", e.toString(), payload);
      // Here you can add DLT / retry topic
    }
  }
}
