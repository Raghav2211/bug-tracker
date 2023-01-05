package com.github.devraghav.bugtracker.issue.validation;

import reactor.core.publisher.Mono;

public interface ValidationStrategy<T> {
  Mono<T> validate(final T t);
}
