package com.github.devraghav.bugtracker.issue.config;

import java.util.Map;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.sender.SenderOptions;

@Configuration
public class KafkaConfig {
  @Bean
  public ReactiveKafkaProducerTemplate<String, SpecificRecordBase> reactiveKafkaProducerTemplate(
      KafkaProperties properties) {
    Map<String, Object> props = properties.buildProducerProperties();
    return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(props));
  }
}
