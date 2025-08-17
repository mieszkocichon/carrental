package com.example.worker.kafka;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class CommandListener {
  private static final Logger log = LoggerFactory.getLogger(CommandListener.class);

  @KafkaListener(topics = "reservations.commands", groupId = "inventory-workers")
  public void onMessage(@Payload String payload) {
    log.info("received_command payload={}", payload);
  }
}
