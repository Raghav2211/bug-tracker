package com.github.devraghav.bugtracker.event.internal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventSpringDIConfig {

  @Bean
  public EventBus.ReactiveMessageBroker defaultMessageBroker() {
    return new DefaultReactiveMessageBroker();
  }
}
