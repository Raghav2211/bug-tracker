package com.github.devraghav.bugtracker.project.validation;

import reactor.core.publisher.Mono;

public interface Validator<T> {
  Mono<T> validate(final T t);
}
