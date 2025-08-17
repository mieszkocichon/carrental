package com.example.booking.service;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommandPublisher {
  private final KafkaTemplate<String, String> kafkaTemplate;
  public void publish(String key, String payload) {
    kafkaTemplate.send("reservations.commands", key, payload);
  }
}
