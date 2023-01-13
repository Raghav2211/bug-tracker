package com.github.devraghav.bugtracker.event.internal;

public class EventBusException extends RuntimeException {
  public EventBusException(String message) {
    super(message);
  }
}
