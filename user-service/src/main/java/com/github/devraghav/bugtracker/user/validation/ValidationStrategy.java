package com.github.devraghav.bugtracker.user.validation;

import reactor.core.publisher.Mono;

public interface ValidationStrategy<T> {
  Mono<T> validate(final T t);
}
