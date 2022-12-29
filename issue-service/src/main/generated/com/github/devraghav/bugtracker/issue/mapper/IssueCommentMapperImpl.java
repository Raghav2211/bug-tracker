package com.github.devraghav.bugtracker.issue.mapper;

import com.github.devraghav.bugtracker.issue.dto.CreateCommentRequest;
import com.github.devraghav.bugtracker.issue.dto.IssueComment;
import com.github.devraghav.bugtracker.issue.entity.IssueCommentEntity;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-12-28T19:29:30+0530",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 17.0.5 (Oracle Corporation)"
)
@Component
public class IssueCommentMapperImpl implements IssueCommentMapper {

    @Override
    public IssueCommentEntity requestToEntity(String issueId, CreateCommentRequest createCommentRequest) {
        if ( issueId == null && createCommentRequest == null ) {
            return null;
        }

        IssueCommentEntity issueCommentEntity = new IssueCommentEntity();

        if ( createCommentRequest != null ) {
            issueCommentEntity.setUserId( createCommentRequest.userId() );
            issueCommentEntity.setContent( createCommentRequest.content() );
        }
        issueCommentEntity.setIssueId( issueId );
        issueCommentEntity.setId( UUID.randomUUID().toString() );
        issueCommentEntity.setCreatedAt( LocalDateTime.now() );

        return issueCommentEntity;
    }

    @Override
    public IssueComment.IssueCommentBuilder entityToResponse(IssueCommentEntity issueCommentEntity) {
        if ( issueCommentEntity == null ) {
            return null;
        }

        IssueComment.IssueCommentBuilder issueCommentBuilder = new IssueComment.IssueCommentBuilder();

        issueCommentBuilder.id( issueCommentEntity.getId() );
        issueCommentBuilder.content( issueCommentEntity.getContent() );
        issueCommentBuilder.createdAt( issueCommentEntity.getCreatedAt() );

        return issueCommentBuilder;
    }
}
