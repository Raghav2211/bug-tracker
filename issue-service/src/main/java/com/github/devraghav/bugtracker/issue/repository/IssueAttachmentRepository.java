package com.github.devraghav.bugtracker.issue.repository;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Mono;

public interface IssueAttachmentRepository {

  Mono<String> upload(String issueId ,String filename, Publisher<DataBuffer> content);
}
