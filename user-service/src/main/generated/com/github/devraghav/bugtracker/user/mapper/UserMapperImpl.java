package com.github.devraghav.bugtracker.user.mapper;

import com.github.devraghav.bugtracker.user.dto.AccessLevel;
import com.github.devraghav.bugtracker.user.dto.CreateUserRequest;
import com.github.devraghav.bugtracker.user.dto.User;
import com.github.devraghav.bugtracker.user.entity.UserEntity;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-12-30T17:18:42+0530",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 17.0.5 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserEntity requestToEntity(CreateUserRequest createUserRequest) {
        if ( createUserRequest == null ) {
            return null;
        }

        UserEntity userEntity = new UserEntity();

        userEntity.setAccess( accessLevelToValue( createUserRequest.access() ) );
        userEntity.setPassword( createUserRequest.password() );
        if ( createUserRequest.hasFirstName() ) {
            userEntity.setFirstName( createUserRequest.firstName() );
        }
        if ( createUserRequest.hasLastName() ) {
            userEntity.setLastName( createUserRequest.lastName() );
        }
        userEntity.setEmail( createUserRequest.email() );

        userEntity.setId( UUID.randomUUID().toString() );
        userEntity.setEnabled( true );

        return userEntity;
    }

    @Override
    public User entityToResponse(UserEntity userEntity) {
        if ( userEntity == null ) {
            return null;
        }

        AccessLevel access = null;
        String id = null;
        String firstName = null;
        String lastName = null;
        String email = null;
        Boolean enabled = null;

        access = valueToAccessLevel( userEntity.getAccess() );
        id = userEntity.getId();
        firstName = userEntity.getFirstName();
        lastName = userEntity.getLastName();
        email = userEntity.getEmail();
        enabled = userEntity.getEnabled();

        User user = new User( id, access, firstName, lastName, email, enabled );

        return user;
    }
}
