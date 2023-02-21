package com.github.devraghav.bugtracker.user.mapper;

import com.github.devraghav.bugtracker.user.entity.UserEntity;
import com.github.devraghav.bugtracker.user.request.UserRequest;
import com.github.devraghav.bugtracker.user.response.UserResponse;
import java.util.Optional;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(
    componentModel = "spring",
    imports = {UUID.class, Optional.class})
public abstract class UserMapper {
  @Autowired private PasswordEncoder passwordEncoder;

  @Mappings({
    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())"),
    @Mapping(target = "enabled", constant = "true"),
    @Mapping(target = "role", source = "role", qualifiedByName = "roleToValue"),
    @Mapping(target = "password", source = "password", qualifiedByName = "encodePassword")
  })
  public abstract UserEntity requestToEntity(UserRequest.CreateUser createUserUserRequest);

  @Mappings({
    @Mapping(
        target = "firstName",
        expression =
            "java(Optional.ofNullable(updateUser.firstName()).orElse(userEntity.getFirstName()))"),
    @Mapping(
        target = "lastName",
        expression =
            "java(Optional.ofNullable(updateUser.lastName()).orElse(userEntity.getLastName()))"),
    @Mapping(
        target = "role",
        expression =
            "java(Optional.ofNullable(updateUser.role()).map(UserRequest.Role::getValue).orElseGet(userEntity::getRole))")
  })
  public abstract UserEntity requestToEntity(
      UserEntity userEntity, UserRequest.UpdateUser updateUser);

  @Mappings({@Mapping(target = "role", source = "role", qualifiedByName = "valueToRole")})
  public abstract UserResponse.User entityToResponse(UserEntity userEntity);

  @Named("roleToValue")
  Integer roleToValue(UserRequest.Role role) {
    return role.getValue();
  }

  @Named("encodePassword")
  String getEncodePassword(String password) {
    return passwordEncoder.encode(password);
  }

  @Named("valueToRole")
  UserRequest.Role valueToRole(Integer roleValue) {
    return UserRequest.Role.fromValue(roleValue);
  }
}
