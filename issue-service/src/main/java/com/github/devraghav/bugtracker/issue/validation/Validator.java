package com.github.devraghav.bugtracker.issue.validation;

import reactor.core.publisher.Mono;

public interface Validator<T, R> {
  Mono<R> validate(final T t);
}
