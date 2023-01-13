package com.github.devraghav.bugtracker.issue.mapper;

import com.github.devraghav.bugtracker.issue.dto.Issue;
import com.github.devraghav.bugtracker.issue.dto.IssueRequests;
import com.github.devraghav.bugtracker.issue.dto.Priority;
import com.github.devraghav.bugtracker.issue.dto.ProjectInfo;
import com.github.devraghav.bugtracker.issue.dto.Severity;
import com.github.devraghav.bugtracker.issue.entity.IssueEntity;
import com.github.devraghav.bugtracker.issue.entity.ProjectInfoRef;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2023-01-13T14:02:50+0530",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 17.0.5 (Oracle Corporation)"
)
@Component
public class IssueMapperImpl implements IssueMapper {

    @Override
    public IssueEntity issueRequestToIssueEntity(IssueRequests.Create createIssueRequest) {
        if ( createIssueRequest == null ) {
            return null;
        }

        IssueEntity issueEntity = new IssueEntity();

        issueEntity.setPriority( priorityToValue( createIssueRequest.priority() ) );
        issueEntity.setSeverity( severityToValue( createIssueRequest.severity() ) );
        issueEntity.setBusinessUnit( createIssueRequest.businessUnit() );
        issueEntity.setProjects( projectInfoSetToProjectInfoRefSet( createIssueRequest.projects() ) );
        issueEntity.setHeader( createIssueRequest.header() );
        issueEntity.setDescription( createIssueRequest.description() );
        issueEntity.setReporter( createIssueRequest.reporter() );
        Map<String, String> map = createIssueRequest.tags();
        if ( map != null ) {
            issueEntity.setTags( new LinkedHashMap<String, String>( map ) );
        }

        issueEntity.setId( UUID.randomUUID().toString() );
        issueEntity.setCreatedAt( LocalDateTime.now() );
        issueEntity.setWatchers( Set.of() );

        return issueEntity;
    }

    @Override
    public IssueEntity issueRequestToIssueEntity(IssueEntity issueEntity, IssueRequests.Update updateIssueRequest) {
        if ( issueEntity == null && updateIssueRequest == null ) {
            return null;
        }

        IssueEntity issueEntity1 = new IssueEntity();

        if ( issueEntity != null ) {
            issueEntity1.setId( issueEntity.getId() );
            Set<ProjectInfoRef> set = issueEntity.getProjects();
            if ( set != null ) {
                issueEntity1.setProjects( new LinkedHashSet<ProjectInfoRef>( set ) );
            }
            issueEntity1.setReporter( issueEntity.getReporter() );
            Set<String> set1 = issueEntity.getWatchers();
            if ( set1 != null ) {
                issueEntity1.setWatchers( new LinkedHashSet<String>( set1 ) );
            }
            issueEntity1.setCreatedAt( issueEntity.getCreatedAt() );
            issueEntity1.setEndedAt( issueEntity.getEndedAt() );
        }
        issueEntity1.setPriority( Optional.ofNullable(updateIssueRequest.priority()).map(Priority::getValue).orElse(issueEntity.getPriority()) );
        issueEntity1.setSeverity( Optional.ofNullable(updateIssueRequest.severity()).map(Severity::getValue).orElse(issueEntity.getSeverity()) );
        issueEntity1.setBusinessUnit( Optional.ofNullable(updateIssueRequest.businessUnit()).orElse(issueEntity.getBusinessUnit()) );
        issueEntity1.setHeader( Optional.ofNullable(updateIssueRequest.header()).orElse(issueEntity.getHeader()) );
        issueEntity1.setDescription( Optional.ofNullable(updateIssueRequest.description()).orElse(issueEntity.getDescription()) );
        issueEntity1.setTags( Optional.ofNullable(updateIssueRequest.tags()).orElse(issueEntity.getTags()) );

        return issueEntity1;
    }

    @Override
    public Issue.IssueBuilder issueEntityToIssue(IssueEntity issueEntity) {
        if ( issueEntity == null ) {
            return null;
        }

        Issue.IssueBuilder issueBuilder = new Issue.IssueBuilder();

        issueBuilder.priority( valueToPriority( issueEntity.getPriority() ) );
        issueBuilder.severity( valueToSeverity( issueEntity.getSeverity() ) );
        issueBuilder.id( issueEntity.getId() );
        issueBuilder.businessUnit( issueEntity.getBusinessUnit() );
        issueBuilder.header( issueEntity.getHeader() );
        issueBuilder.description( issueEntity.getDescription() );
        Map<String, String> map = issueEntity.getTags();
        if ( map != null ) {
            issueBuilder.tags( new LinkedHashMap<String, String>( map ) );
        }
        issueBuilder.createdAt( issueEntity.getCreatedAt() );
        issueBuilder.endedAt( issueEntity.getEndedAt() );

        return issueBuilder;
    }

    protected ProjectInfoRef projectInfoToProjectInfoRef(ProjectInfo projectInfo) {
        if ( projectInfo == null ) {
            return null;
        }

        ProjectInfoRef projectInfoRef = new ProjectInfoRef();

        projectInfoRef.setProjectId( projectInfo.projectId() );
        projectInfoRef.setVersionId( projectInfo.versionId() );

        return projectInfoRef;
    }

    protected Set<ProjectInfoRef> projectInfoSetToProjectInfoRefSet(Set<ProjectInfo> set) {
        if ( set == null ) {
            return null;
        }

        Set<ProjectInfoRef> set1 = new LinkedHashSet<ProjectInfoRef>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( ProjectInfo projectInfo : set ) {
            set1.add( projectInfoToProjectInfoRef( projectInfo ) );
        }

        return set1;
    }
}
