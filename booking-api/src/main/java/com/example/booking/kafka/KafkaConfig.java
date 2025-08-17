package com.example.booking.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
  @Bean
  public NewTopic reservationCommands() {
    return TopicBuilder.name("reservations.commands")
            .partitions(12)
            .replicas(1)
            .build();
  }
}
