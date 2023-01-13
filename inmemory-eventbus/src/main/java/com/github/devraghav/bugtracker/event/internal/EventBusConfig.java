package com.github.devraghav.bugtracker.event.internal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventBusConfig {

  @Bean
  public EventBus.ReactiveMessageBroker messageBroker() {
    return new DefaultReactiveMessageBroker();
  }
}
