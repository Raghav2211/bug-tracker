package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.issue.dto.Comment;
import com.github.devraghav.bugtracker.issue.pubsub.ReactiveMessageBroker;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

@Component
public class CommentStreamMessageBroker implements ReactiveMessageBroker<Comment> {
  private Sinks.Many<Comment> sink =
      Sinks.many()
          .multicast()
          .onBackpressureBuffer(
              Queues.SMALL_BUFFER_SIZE,
              false); // false will prevent to close the sink if last subscriber cancel the
  // subscription

  @Override
  public Sinks.Many<Comment> getWriteChannel() {
    return sink;
  }
}
