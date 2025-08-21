package com.example.booking.api;

import com.example.booking.service.CommandPublisher;
import com.example.booking.service.IdempotencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

  private final CommandPublisher commandPublisher;
  private final IdempotencyService idempotency;
  private final ObjectMapper mapper = new ObjectMapper();

  public ReservationController(CommandPublisher commandPublisher, IdempotencyService idempotency) {
    this.commandPublisher = commandPublisher;
    this.idempotency = idempotency;
  }

  @PostMapping
  public ResponseEntity<?> create(
          @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
          @RequestBody Map<String, String> body) throws Exception {

    UUID reservationId = UUID.randomUUID();
    UUID customerId = UUID.fromString(body.get("customerId"));
    UUID vehicleId = UUID.fromString(body.get("vehicleId"));
    Instant pickup = Instant.parse(body.get("pickupTime"));
    Instant ret = Instant.parse(body.get("returnTime"));

    // API idempotency (if the client provided an Idempotency-Key)
    if (idempotencyKey != null && !idempotencyKey.isBlank()) {
      String remembered = idempotency.rememberOrGet(idempotencyKey, reservationId.toString());
      reservationId = UUID.fromString(remembered);
    }

    var payload = Map.of(
            "type", "CreateReservation",
            "reservationId", reservationId.toString(),
            "customerId", customerId.toString(),
            "vehicleId", vehicleId.toString(),
            "pickupTime", pickup.toString(),
            "returnTime", ret.toString(),
            "requestedAt", Instant.now().toString()
    );

    commandPublisher.publish(vehicleId.toString(), mapper.writeValueAsString(payload));

    return ResponseEntity.accepted().body(Map.of("reservationId", reservationId));
  }
}