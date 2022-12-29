package com.github.devraghav.bugtracker.issue.repository;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class CustomIssueAttachmentRepository implements IssueAttachmentRepository {
  private final ReactiveGridFsTemplate reactiveGridFsTemplate;

  @Override
  public Mono<String> upload(String issueId ,String filename,Publisher<DataBuffer> content) {
    var metadData = new Document();
    metadData.append("issueId" ,issueId );
    return reactiveGridFsTemplate.store(content, filename, metadData).map(ObjectId::toHexString);
  }
}
