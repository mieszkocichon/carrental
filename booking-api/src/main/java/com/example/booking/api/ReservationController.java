package com.example.booking.api;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper mapper = new ObjectMapper();

  @Value("${spring.application.name:booking-api}") private String appName;

  @Autowired
  public ReservationController(KafkaTemplate<String, String> kafkaTemplate) { this.kafkaTemplate = kafkaTemplate; }

  @PostMapping
  public ResponseEntity<?> create(@RequestBody Map<String, String> body) throws Exception {
    UUID reservationId = UUID.randomUUID();
    UUID customerId = UUID.fromString(body.get("customerId"));
    UUID vehicleId = UUID.fromString(body.get("vehicleId"));
    Instant pickup = Instant.parse(body.get("pickupTime"));
    Instant ret = Instant.parse(body.get("returnTime"));

    var payload = Map.of(
      "type", "CreateReservation",
      "reservationId", reservationId.toString(),
      "customerId", customerId.toString(),
      "vehicleId", vehicleId.toString(),
      "pickupTime", pickup.toString(),
      "returnTime", ret.toString(),
      "requestedAt", Instant.now().toString(),
      "sourceApp", appName
    );

    kafkaTemplate.send("reservations.commands", vehicleId.toString(), mapper.writeValueAsString(payload));
    return ResponseEntity.accepted().body(Map.of("reservationId", reservationId));
  }
}
