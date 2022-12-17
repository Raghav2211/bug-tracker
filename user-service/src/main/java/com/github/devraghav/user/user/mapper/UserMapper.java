package com.github.devraghav.user.user.mapper;

import com.github.devraghav.user.user.dto.AccessLevel;
import com.github.devraghav.user.user.dto.User;
import com.github.devraghav.user.user.dto.UserException;
import com.github.devraghav.user.user.dto.UserRequest;
import com.github.devraghav.user.user.entity.UserEntity;
import java.util.UUID;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    imports = {UUID.class})
public interface UserMapper {

  @Mappings({
    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())"),
    @Mapping(target = "enabled", constant = "true"),
    @Mapping(target = "access", source = "access", qualifiedByName = "accessLevelToValue")
  })
  UserEntity requestToEntity(UserRequest userRequest);

  @Mappings({
    @Mapping(target = "access", source = "access", qualifiedByName = "valueToAccessLevel")
  })
  User entityToResponse(UserEntity userEntity);

  @Named("accessLevelToValue")
  default Integer accessLevelToValue(AccessLevel accessLevel) {
    return switch (accessLevel) {
      case READ -> AccessLevel.READ.getValue();
      case WRITE -> AccessLevel.WRITE.getValue();
      case ADMIN -> AccessLevel.ADMIN.getValue();
    };
  }

  @Named("valueToAccessLevel")
  default AccessLevel valueToAccessLevel(Integer accessLevelValue) {
    return switch (accessLevelValue) {
      case 1 -> AccessLevel.READ;
      case 2 -> AccessLevel.WRITE;
      case 0 -> AccessLevel.ADMIN;
      default -> throw UserException.unrecognizedAccessLevel();
    };
  }
}
