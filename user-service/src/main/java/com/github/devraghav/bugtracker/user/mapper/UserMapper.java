package com.github.devraghav.bugtracker.user.mapper;

import com.github.devraghav.bugtracker.user.dto.Role;
import com.github.devraghav.bugtracker.user.dto.User;
import com.github.devraghav.bugtracker.user.dto.UserRequest;
import com.github.devraghav.bugtracker.user.entity.UserEntity;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(
    componentModel = "spring",
    imports = {UUID.class})
public abstract class UserMapper {
  @Autowired private PasswordEncoder passwordEncoder;

  @Mappings({
    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())"),
    @Mapping(target = "enabled", constant = "true"),
    @Mapping(target = "role", source = "role", qualifiedByName = "roleToValue"),
    @Mapping(target = "password", source = "password", qualifiedByName = "encodePassword")
  })
  public abstract UserEntity requestToEntity(UserRequest.Create createUserRequest);

  @Mappings({@Mapping(target = "role", source = "role", qualifiedByName = "valueToRole")})
  public abstract User entityToResponse(UserEntity userEntity);

  @Named("roleToValue")
  Integer roleToValue(Role role) {
    return role.getValue();
  }

  @Named("encodePassword")
  String getEncodePassword(String password) {
    return passwordEncoder.encode(password);
  }

  @Named("valueToRole")
  Role valueToRole(Integer roleValue) {
    return Role.fromValue(roleValue);
  }
}
