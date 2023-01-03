package com.github.devraghav.bugtracker.user.event;

public interface Publisher<T> {
  void publish(T message);
}
