package com.github.devraghav.bugtracker.issue.mapper;

import com.github.devraghav.bugtracker.issue.dto.Comment;
import com.github.devraghav.bugtracker.issue.dto.CreateCommentRequest;
import com.github.devraghav.bugtracker.issue.entity.CommentEntity;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2023-01-07T00:29:07+0530",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 17.0.5 (Oracle Corporation)"
)
@Component
public class CommentMapperImpl implements CommentMapper {

    @Override
    public CommentEntity requestToEntity(CreateCommentRequest createCommentRequest) {
        if ( createCommentRequest == null ) {
            return null;
        }

        CommentEntity commentEntity = new CommentEntity();

        commentEntity.setIssueId( createCommentRequest.issueId() );
        commentEntity.setUserId( createCommentRequest.userId() );
        commentEntity.setContent( createCommentRequest.content() );

        commentEntity.setId( UUID.randomUUID().toString() );
        commentEntity.setCreatedAt( LocalDateTime.now() );

        return commentEntity;
    }

    @Override
    public Comment.CommentBuilder entityToResponse(CommentEntity commentEntity) {
        if ( commentEntity == null ) {
            return null;
        }

        Comment.CommentBuilder commentBuilder = new Comment.CommentBuilder();

        commentBuilder.id( commentEntity.getId() );
        commentBuilder.content( commentEntity.getContent() );
        commentBuilder.createdAt( commentEntity.getCreatedAt() );

        return commentBuilder;
    }
}
